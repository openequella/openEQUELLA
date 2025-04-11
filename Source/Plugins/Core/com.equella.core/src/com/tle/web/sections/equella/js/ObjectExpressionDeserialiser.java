/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0, (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.web.sections.equella.js;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.google.common.collect.Lists;
import com.tle.web.sections.js.generic.expression.ArrayExpression;
import com.tle.web.sections.js.generic.expression.NullExpression;
import com.tle.web.sections.js.generic.expression.ObjectExpression;
import com.tle.web.sections.js.generic.expression.PrimitiveValueExpression;
import com.tle.web.sections.js.generic.expression.StringExpression;
import java.io.IOException;
import java.util.List;

public class ObjectExpressionDeserialiser extends StdDeserializer<ObjectExpression> {
  public ObjectExpressionDeserialiser() {
    super(ObjectExpression.class);
  }

  protected void throwMappingException(
      DeserializationContext context, JsonToken token, JsonParser parser, String message)
      throws IOException {
    context.handleUnexpectedToken(
        ObjectExpression.class, token, parser, message + " " + where(parser, context));
  }

  @Override
  public ObjectExpression deserialize(JsonParser parser, DeserializationContext context)
      throws IOException, JsonProcessingException {
    final ObjectExpression map = new ObjectExpression();
    int times = 0;
    while (parser.hasCurrentToken()) {
      final JsonToken currentToken = parser.getCurrentToken();
      switch (currentToken) {
        case START_OBJECT:
          // sure
          times++;
          if (times > 1) {
            throwMappingException(context, currentToken, parser, "Object opened twice??");
          }
          break;

        case END_OBJECT:
          return map;

        case FIELD_NAME:
          parser.nextToken();
          map.put(parser.currentName(), parse(parser, context));
          break;

        case NOT_AVAILABLE:
          parser.nextToken();
          break;

        default:
          throwMappingException(context, currentToken, parser, "Object can only contain fields");
      }
      parser.nextToken();
    }
    throwMappingException(context, null, parser, "No end object encountered");
    // Never reached.
    return null;
  }

  private static String where(JsonParser parser, DeserializationContext context) {
    return "";
  }

  private Object parse(JsonParser parser, DeserializationContext context)
      throws IOException, JsonProcessingException {
    final JsonToken currentToken = parser.getCurrentToken();
    switch (currentToken) {
      case START_OBJECT:
        final ObjectExpression oe = new ObjectExpressionDeserialiser().deserialize(parser, context);
        final JsonToken tokenAfterParseObject = parser.getCurrentToken();
        if (tokenAfterParseObject != JsonToken.END_OBJECT) {
          throwMappingException(context, tokenAfterParseObject, parser, "Should be on end object");
        }
        return oe;

      case VALUE_FALSE:
      case VALUE_TRUE:
        return new PrimitiveValueExpression(parser.getValueAsBoolean());

      case VALUE_NUMBER_INT:
        return new PrimitiveValueExpression(parser.getValueAsInt());

      case VALUE_NUMBER_FLOAT:
        return new PrimitiveValueExpression(parser.getValueAsDouble());

      case VALUE_NULL:
        return new NullExpression();

      case VALUE_STRING:
        return new StringExpression(parser.getText());

      case START_ARRAY:
        final ArrayExpression ae = parseArray(parser, context);
        final JsonToken tokenAfterParseArray = parser.getCurrentToken();
        if (tokenAfterParseArray != JsonToken.END_ARRAY) {
          throwMappingException(context, tokenAfterParseArray, parser, "Should be on end array");
        }
        return ae;

      case END_ARRAY:
      case END_OBJECT:
        // whoops
        throwMappingException(context, currentToken, parser, "Unexpected end object/array");

      case FIELD_NAME:
      case NOT_AVAILABLE:
        throwMappingException(context, currentToken, parser, "How?");

      default:
        throwMappingException(context, currentToken, parser, "Unhandled token");
        // Never reached.
        return null;
    }
  }

  private ArrayExpression parseArray(JsonParser parser, DeserializationContext context)
      throws IOException, JsonProcessingException {
    boolean started = false;
    final List<Object> values = Lists.newArrayList();
    while (parser.hasCurrentToken()) {
      final JsonToken currentToken = parser.getCurrentToken();
      switch (currentToken) {
        case START_ARRAY:
          if (started) {
            values.add(parseArray(parser, context));
          }
          started = true;
          break;
        case END_ARRAY:
          if (!started) {
            throwMappingException(context, null, parser, "End array encountered without start");
          }
          return new ArrayExpression(values.toArray());
        case NOT_AVAILABLE:
          break;
        default:
          if (!started) {
            throwMappingException(context, null, parser, "Value encountered without array start");
          }
          values.add(parse(parser, context));
      }
      parser.nextToken();
    }
    throwMappingException(context, null, parser, "No end array found");
    // Never reached.
    return null;
  }
}
