/*
* MIT License
*
* Copyright (c) 2020 Trevor Flynn
*
* Permission is hereby granted, free of charge, to any person obtaining a copy
* of this software and associated documentation files (the "Software"), to deal
* in the Software without restriction, including without limitation the rights
* to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
* copies of the Software, and to permit persons to whom the Software is
* furnished to do so, subject to the following conditions:
*
* The above copyright notice and this permission notice shall be included in all
* copies or substantial portions of the Software.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
* SOFTWARE.
*/
package io.tlf.monkeynetty.test.messages;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Radosław K <oxplay.net@gmail.com>
 * 
 * Class used for message testing purpose, it will check:
 * - if Serializable Object within message will work correctly
 * - if default Java Serializable class will not cause issue
 */
public class TestSerializableDataA implements Serializable {
    
    public List<Long> list;

    public TestSerializableDataA() {
        list = new ArrayList<>();
        for (long i = 0; i < 10; i++) {
            list.add(i);
        }
    }

    @Override
    public String toString() {
        return list.toString();
    }
}
