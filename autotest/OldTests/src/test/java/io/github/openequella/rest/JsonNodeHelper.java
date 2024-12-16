package io.github.openequella.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.Streams;
import java.util.Optional;
import java.util.function.Function;

/** A helper class contains some functions to manipulate JsonNode. */
public class JsonNodeHelper {
  /**
   * A helper function to get sub JsonNode from a given Array JsonNode. See {@link
   * #getNode(ArrayNode, String, String)} for an example.
   *
   * @param result The Array JsonNode to search from.
   * @param getAttributeValue A function to get the attribute value from a JsonNode.
   * @param expectedValue The expected value of the attribute in the node you want.
   */
  static JsonNode getNode(
      ArrayNode result, Function<JsonNode, JsonNode> getAttributeValue, String expectedValue) {
    Optional<JsonNode> node =
        Streams.stream(result.elements())
            .filter(
                n ->
                    Optional.ofNullable(getAttributeValue.apply(n))
                        .map(JsonNode::asText)
                        .map(attribute -> attribute.equals(expectedValue))
                        .orElse(false))
            .findFirst();
    return node.orElseThrow();
  }

  /**
   * A helper function to get corresponding node from a given Array JsonNode. For example, if we
   * have a JsonNode:
   *
   * <pre>{@code
   * [
   *   {
   *     "attributeName": "name1",
   *     "other":"1"
   *   },
   *   {
   *     "attributeName": "name2",
   *     "other":"2"
   *   }
   * ]
   * }</pre>
   *
   * With "getNode(result, "attributeName", "name1")", it will return the first node.
   */
  static JsonNode getNode(ArrayNode result, String attributeName, String expectedValue) {
    return getNode(result, n -> n.get(attributeName), expectedValue);
  }
}
