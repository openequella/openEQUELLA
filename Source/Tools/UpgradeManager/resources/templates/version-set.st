version-set(heading, versions) ::= <<
$heading-content(
  heading=heading,
  content={
    $if(versions)$
    <ul>
      $versions:{it |
        <li>
          <input
            id="version$it.semanticVersion$"
            class="version-item"
            type="radio"
            name="version"
            value="$it.filename$"
          />
          <label for="version$it.semanticVersion$">
            $it.displayName$
            <span style="color: #AAAAAA;"> ($it.semanticVersion$) </span>
          </label>
        </li>
      }$
    </ul>
    $else$ No versions found
    $endif$
  }
)$
>>
