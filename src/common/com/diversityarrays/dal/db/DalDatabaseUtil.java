/*
 * dalserver-interop library - implementation of DAL server for interoperability
 * Copyright (C) 2015  Diversity Arrays Technology
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.diversityarrays.dal.db;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.persistence.Column;

import org.apache.commons.codec.binary.Hex;

import com.diversityarrays.dal.entity.DalEntity;
import com.diversityarrays.dal.ops.DalOperation;
import com.diversityarrays.dal.ops.OperationMatch;
import com.diversityarrays.dal.ops.WordNode;
import com.diversityarrays.dalclient.DalUtil;

public class DalDatabaseUtil {
	
	static public String getFilteringClause(Map<String, String> methodParms) {
		String result = null;
		if (methodParms != null) {
			result = methodParms.get(DalOperation.OPTION_FILTERING);
			if (result != null && result.trim().isEmpty()) {
				result = null;
			}
		}
		return result;
	}

	/**
	 * Calculate an RFC 2104 compliant HMAC signature.
	 * @param key is the signing key
	 * @param data is the data to be signed 
	 * @return the base64-encoded signature as a String
	 */
	public static String computeHmacSHA1(String key, String data) {
		try {
			byte[] keyBytes = key.getBytes("UTF-8");           
			SecretKeySpec signingKey = new SecretKeySpec(keyBytes, "HmacSHA1");

			Mac mac = Mac.getInstance("HmacSHA1");
			mac.init(signingKey);

			byte[] rawHmac = mac.doFinal(data.getBytes("UTF-8"));

			// TODO Consider replacing with a simple hex encoder so we don't need commons-codec
			byte[] hexBytes = new Hex().encode(rawHmac);

			return new String(hexBytes, "UTF-8");

		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		} catch (InvalidKeyException e) {
			throw new RuntimeException(e);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	static public OperationMatch findOperationMatch(
			String dalcmd, WordNode root, StringBuilder errmsg) 
	{
		String[] parts = dalcmd.split("/");
		
		OperationMatch result = new OperationMatch();

		Pattern digitsCsv = Pattern.compile("^(\\d+)(,[\\d,]+)+$"); // "^[\\d]+(,[\\d+])+$"
		Pattern digits = Pattern.compile("[\\d]+");
		Pattern chars = Pattern.compile("[a-zA-Z]+");
		int nParts = parts.length;

		WordNode node = root;
		for (int pi = 0; pi < nParts; ++pi) {
			String p = parts[pi];
			WordNode tmp = node.lookup(p);
			if (tmp == null) {
				if (!digits.matcher(p).matches()) {
					if (!digitsCsv.matcher(p).matches()) {
						// it isn't numeric
						if (!node.hasNonNumericParameters()) {
							errmsg.append("Mismatch at position ").append(pi)
									.append(": '").append(p).append("'");
							node = null;
							break;
						}
						// so this *is* a possible match, stick with the current
						// node
					}
				}
				if (!node.hasParameters()) {
					errmsg.append("No parameter at position ").append(pi);
					node = null;
					break; // No parameters
				}
				// Ok. It is a parameter, we'll continue
				result.addParameterValue(p);
			} else {
				node = tmp;
			}
		}

		if (node != null) {
			result.node = node;
		}

		return result;
	}

	public static String getUsernamePasswordErrorMessage(String username, String password, Map<String,String> parms) 
	{
		final String parms_rand_num = parms.get("rand_num");
		if (parms_rand_num == null || parms_rand_num.isEmpty()) {
			return "Missing parameter 'rand_num'";
		}
		
		final String parms_url = parms.get("url");
		if (parms_rand_num == null || parms_rand_num.isEmpty()) {
			return "Missing parameter 'url'";
		}

		final String parms_signature = parms.get("signature");
		if (parms_signature == null || parms_signature.isEmpty()) {
			return "Missing parameter 'signature'";
		}

		String pwdUnameHash = DalUtil.computeHmacSHA1(password, username);
		String randhash = DalUtil.computeHmacSHA1(pwdUnameHash, parms_rand_num);
		String signature = DalUtil.computeHmacSHA1(randhash, parms_url);
		
		return signature.equals(parms_signature) ? null : "Invalid username or password";
	}
	
	static public void addEntityFields(Class<? extends DalEntity> entityClass, DalResponseBuilder responseBuilder) {

		responseBuilder.addResponseMeta("SCol");
		
		for (Field fld : entityClass.getDeclaredFields()) {
			if (! Modifier.isStatic(fld.getModifiers())) {
				Column column = fld.getAnnotation(Column.class);
				if (column != null) {
					
					DalResponseBuilder builder = responseBuilder.startTag("SCol");
					
					builder.attribute("Required", column.nullable() ? "0" : "1");
					
					int colSize = 11;
					Class<?> fieldType = fld.getType();
					if (String.class == fieldType) {
						colSize = column.length();
					}
					builder.attribute("ColSize", Integer.toString(colSize));

					builder.attribute("Description", "");

					builder.attribute("Name", column.name());

					// TODO Synchronise with the Perl DAL code
					builder.attribute("DataType", fieldType.getSimpleName().toLowerCase());
					
					builder.endTag();
				}
			}
		}
	}
	
	
//	static public Map<String,Column> buildColumnByName(Class<? extends DalEntity> entityClass) {
//		Map<String,Column> columnByName = new LinkedHashMap<String,Column>();
//		
//		for (Field fld : entityClass.getDeclaredFields()) {
//			if (! Modifier.isStatic(fld.getModifiers())) {
//				Column column = fld.getAnnotation(Column.class);
//				fld.setAccessible(true);
//				columnByName.put(column.name(), column);
//			}
//		}
//		
//		return columnByName;
//	}
	
	static public Map<Field,Column> buildEntityFieldColumnMap(Class<? extends DalEntity> entityClass) {
		Map<Field,Column> columnByField = new LinkedHashMap<Field, Column>();
		
		for (Field fld : entityClass.getDeclaredFields()) {
			if (! Modifier.isStatic(fld.getModifiers())) {
				Column column = fld.getAnnotation(Column.class);
				fld.setAccessible(true);
				columnByField.put(fld, column);
			}
		}
		
		return columnByField;
	}
		
	private DalDatabaseUtil() {
	}



}
