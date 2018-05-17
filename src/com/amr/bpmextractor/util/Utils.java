/*
 * (C) Copyright 2018-present Amr Yassin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors: 	Amr Yassin
 */
package com.amr.bpmextractor.util;

public class Utils {
	
	public static String toCamelCase(String str)
	{
		StringBuilder builder = new StringBuilder(str);
		boolean isLastSpace = true;
		
		for(int i = 0; i < builder.length(); i++)
		{
			char ch = builder.charAt(i);
			
			if(isLastSpace && ch >= 'a' && ch <='z')
			{
				builder.setCharAt(i, (char)(ch - 32));
				isLastSpace = false;
			}
			else if (ch != ' ')
				isLastSpace = false;
			else
				isLastSpace = true;
		}
	
		return builder.toString();
	}

}
