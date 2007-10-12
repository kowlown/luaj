/*******************************************************************************
* Copyright (c) 2007 LuaJ. All rights reserved.
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
package lua.debug;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class DebugEvent implements Serializable {    
    
    protected DebugEventType type;

    public DebugEvent(DebugEventType type) {
        this.type = type;
    }
    
    public DebugEventType getType() {
        return type;
    }

    public void setType(DebugEventType type) {
        this.type = type;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return type.toString();
    }
    
    public static void serialize(DataOutputStream out, DebugEvent object)
    throws IOException {
    	SerializationHelper.serialize(object.getType(), out);
	}

	public static DebugEvent deserialize(DataInputStream in) throws IOException {
		DebugEventType type = (DebugEventType) SerializationHelper.deserialize(in);
		return new DebugEvent(type);
	}
}
