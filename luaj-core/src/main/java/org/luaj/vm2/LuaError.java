/*******************************************************************************
* Copyright (c) 2009-2011 Luaj.org. All rights reserved.
*
* Permission is hereby granted, free of charge, to any person obtaining a copy
* of this software and associated documentation files (the "Software"), to deal
* in the Software without restriction, including without limitation the rights
* to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
* copies of the Software, and to permit persons to whom the Software is
* furnished to do so, subject to the following conditions:
*
* The above copyright notice and this permission notice shall be included in
* all copies or substantial portions of the Software.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
* THE SOFTWARE.
******************************************************************************/
package org.luaj.vm2;

/**
 * RuntimeException that is thrown and caught in response to a lua error.
 * <p>
 * {@link LuaError} is used wherever a lua call to {@code error()} would be used
 * within a script.
 * <p>
 * Since it is an unchecked exception inheriting from {@link RuntimeException},
 * Java method signatures do notdeclare this exception, althoug it can be thrown
 * on almost any luaj Java operation. This is analagous to the fact that any lua
 * script can throw a lua error at any time.
 * <p>
 * The LuaError may be constructed with a message object, in which case the
 * message is the string representation of that object. getMessageObject will
 * get the object supplied at construct time, or a LuaString containing the
 * message of an object was not supplied.
 */
public class LuaError extends RuntimeException {
	/**
	 * Thrown when indexing something that isn't indexable (e.g. nil)
	 */
	public static class LuaBadIndexTargetError extends LuaError {
		public final int type;
		public final String typename;
		public final String key;

		public LuaBadIndexTargetError(int type, String typename, String key) {
			// This has to be lowercase for compatibility lmao
			super(String.format("attempt to index ? (a %s value) with key '%s'", typename, key));
			this.type = type;
			this.typename = typename;
			this.key = key;
		}
	}

	/**
	 * Thrown when e.g. trying to call something that isn't callable.
	 */
	public static class LuaBadOperatorTargetError extends LuaError {
        public final String mtTarget;
        public final String opFriendlyName;
        public final int actualType;
        public final String typeName;

        public LuaBadOperatorTargetError(String mtTarget, String opFriendlyName, int actualType, String typeName) {
			super(String.format("attempt to %s a %s value", opFriendlyName, typeName));
            this.mtTarget = mtTarget;
            this.opFriendlyName = opFriendlyName;
            this.actualType = actualType;
            this.typeName = typeName;
        }
	}

	public static class LuaOperationTypeError extends LuaError {
        public final String opFriendlyName;
        public final int leftType;
        public final String leftTypeName;
        public final int rightName;
        public final String rightTypeName;

        public LuaOperationTypeError(String opFriendlyName, int leftType, String leftTypeName, int rightName, String rightTypeName) {
			super(String.format("attempt to %s %s with %s", opFriendlyName, leftType, leftTypeName));
            this.opFriendlyName = opFriendlyName;
            this.leftType = leftType;
            this.leftTypeName = leftTypeName;
            this.rightName = rightName;
            this.rightTypeName = rightTypeName;
        }
	}

	public static class LuaTypeError extends LuaError {
		public final int type;
		public final String typename;
		public final int targetType;
		public final String targetTypeName;

        public LuaTypeError(int type, String typename, int targetType, String targetTypeName) {
            this(type, typename, targetType, targetTypeName, "type");
        }

        public LuaTypeError(int type, String typename, int targetType, String targetTypeName, String kind) {
            super(String.format("%s: %s expected, got %s", kind, targetTypeName, typename));

            this.type = type;
			this.typename = typename;
			this.targetType = targetType;
			this.targetTypeName = targetTypeName;
        }
	}

	public static class LuaVMError extends LuaError {
		public LuaVMError(Throwable cause) {
			super(cause);
		}

		public LuaVMError(String message) {
			super(message);
		}

		public LuaVMError(String message, int level) {
			super(message, level);
		}
	}

	public static class LuaUserError extends LuaError {

		public LuaUserError(Throwable cause) {
			super(cause);
		}

		public LuaUserError(String message) {
			super(message);
		}

		public LuaUserError(String message, int level) {
			super(message, level);
		}

		public LuaUserError(LuaValue message_object) {
			super(message_object);
		}
	}

	private static final long serialVersionUID = 1L;

	protected int level;

	protected String fileline;

	protected String traceback;

	protected Throwable cause;

	private LuaValue object;

	/**
	 * Get the string message if it was supplied, or a string representation of
	 * the message object if that was supplied.
	 */
	@Override
	public String getMessage() {
		if (traceback != null)
			return traceback;
		String m = super.getMessage();
		if (m == null)
			return null;
		if (fileline != null)
			return fileline + " " + m;
		return m;
	}

	/**
	 * Get the LuaValue that was provided in the constructor, or a LuaString
	 * containing the message if it was a string error argument.
	 *
	 * @return LuaValue which was used in the constructor, or a LuaString
	 *         containing the message.
	 */
	public LuaValue getMessageObject() {
		if (object != null)
			return object;
		String m = getMessage();
		return m != null? LuaValue.valueOf(m): null;
	}

	/**
	 * Construct LuaError when a program exception occurs.
	 * <p>
	 * All errors generated from lua code should throw LuaError(String) instead.
	 *
	 * @param cause the Throwable that caused the error, if known.
	 */
	public LuaError(Throwable cause) {
		super("vm error: " + cause);
		this.cause = cause;
		this.level = 1;
	}

	/**
	 * Construct a LuaError with a specific message.
	 *
	 * @param message message to supply
	 */
	public LuaError(String message) {
		super(message);
		this.level = 1;
	}

	/**
	 * Construct a LuaError with a message, and level to draw line number
	 * information from.
	 *
	 * @param message message to supply
	 * @param level   where to supply line info from in call stack
	 */
	public LuaError(String message, int level) {
		super(message);
		this.level = level;
	}

	/**
	 * Construct a LuaError with a LuaValue as the message object, and level to
	 * draw line number information from.
	 *
	 * @param message_object message string or object to supply
	 */
	public LuaError(LuaValue message_object) {
		super(message_object.tojstring());
		this.object = message_object;
		this.level = 1;
	}

	/**
	 * Get the cause, if any.
	 */
	@Override
	public Throwable getCause() { return cause; }

}
