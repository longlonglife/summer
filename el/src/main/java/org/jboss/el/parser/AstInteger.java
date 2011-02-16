/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* Generated By:JJTree: Do not edit this line. AstInteger.java */

package org.jboss.el.parser;

import java.math.BigInteger;

import javax.el.ELException;

import org.jboss.el.lang.EvaluationContext;



/**
 * @author Jacob Hookom [jacob@hookom.net]
 * @version $Change: 181177 $$DateTime: 2001/06/26 08:45:09 $$Author: markt $
 */
@SuppressWarnings("rawtypes")
public final class AstInteger extends SimpleNode {
	public AstInteger(int id) {
		super(id);
	}

	private Number number;

	protected Number getInteger() {
		if (this.number == null) {
			try {
				this.number = new Long(this.image);
			} catch (ArithmeticException e1) {
				this.number = new BigInteger(this.image);
			}
		}
		return number;
	}

	public Class getType(EvaluationContext ctx)
			throws ELException {
		return this.getInteger().getClass();
	}

	public Object getValue(EvaluationContext ctx)
			throws ELException {
		return this.getInteger();
	}
}
