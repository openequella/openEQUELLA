/**
 * Documentation on the Advanced Script Control. <h1>Freemarker Basics</h1>
 * <p>
 * The Advanced Script Control display template and client-side JavaScript text
 * fields accept Freemarker mark-up. You do not have to use Freemarker mark-up
 * if you simply want to display some HTML, although if this is all you are
 * going to do then it is recommended you use the Raw HTML control instead.
 * <p>
 * Freemarker tags are like HTML tags, but with # in front of the tag name.
 * Common Freemarker tags are:
 * <ul>
 * <li><code>&lt;#if>&lt;/#if></code></li>
 * <li><code>&lt;#list someList as someItem>&lt;/#list></code></li>
 * <li><code>&lt;#assign someNewVariable = someExpression /></code></li>
 * </ul>
 * <p>
 * To print the value of a variable the variable needs to be enclosed by a
 * <code>${}</code>, for example: <div class="block">
 * <code>&lt;#if myVariable == true>${myTrueText}&lt;/#if></code></div>
 * <p>
 * For a full Freemarker guide, see <a
 * href="http://freemarker.sourceforge.net/docs/index.html"
 * >http://freemarker.sourceforge.net/docs/index.html</a>
 * <h1>Freemarker Panes</h1>
 * <p>
 * The Freemarker panes are the client side panes, that is, the
 * <em>Display Template</em>, the <em>On-Load JavaScript</em> and the
 * <em>On-Submit JavaScript</em>. All Freemarker variables are referenced by
 * enclosing the name of the variable in <code>${}</code>, for example: <div
 * class="block"><code>${prefix}</code></div> The exception is when you are
 * within a Freemarker tag such as <code>&lt#if><code>, for example: 
 * <div class="block"><code>&lt;#if prefix == 'something'><code></div>
 * <p>
 * All regular script variables are available in the Freemarker panes, such as <code>xml</code>, <code>page</code> and <code>user</code>. These additional variables are
 * available:
 * <dl>
 * <dt><b>prefix</b></dt>
 * <dd>The unique prefix for this control. Any input tags you render should have
 * a name which is prefixed by this variable, otherwise you will not be able to
 * read it back on the server. E.g.: <div class="block">
 * <code>&lt;input id="${prefix}myInput" name="${prefix}myInput" type="text"></code>
 * </div></dd>
 * <dt><b>submitJavascript</b></dt>
 * <dd>This will render JavaScript to submit the page. You would normally use it
 * on a button. E.g.: <div class="block">
 * <code>&lt;input id="${prefix}mySubmit" name="${prefix}mySubmit" type="button" value="Submit Me" onclick="${submitJavascript}"></code>
 * </div></dd>
 * </dl>
 * <h1>Server-side Script Panes</h1>
 * <p>
 * All regular script variables are available in the server-side script panes,
 * such as <code>xml</code>, <code>page</code> and <code>user</code>. The
 * On-Submit script has an additional <code>request</code> variable available to
 * read submitted values from your mark-up. Only values prefixed with the
 * <code>${prefix}</code> variable will be available in the request object. When
 * reading from the request object there is no need to specify the prefix, for
 * example: <div class="block">
 * <code>var myInput = request.get('myInput');<code></div>
 */
package com.tle.web.controls.advancedscript.scripting;