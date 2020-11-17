Liquid Crystal Studios' Java Style Guide  

## Coding style

### Formatting

#### Use line breaks wisely
There are generally two reasons to insert a line break:

1. Your statement exceeds the column limit.

2. You want to logically separate a thought.<br />
Writing code is like telling a story.  Written language constructs like chapters, paragraphs,
and punctuation (e.g. semicolons, commas, periods, hyphens) convey thought hierarchy and
separation.  We have similar constructs in programming languages; you should use them to your
advantage to effectively tell the story to those reading the code.

#### Indent style
We use the "one true brace style" ([1TBS](http://en.wikipedia.org/wiki/Indent_style#Variant:_1TBS)).
Indent size is 4 columns.

```
    // Like this.
    if (x < 0) {
        negative(x);
    } else {
        nonnegative(x);
    }

    // Not like this.
    if (x < 0)
        negative(x);

    // Also not like this.
    if (x < 0) negative(x);
```

Continuation indent is 8 columns.  Nested continuations may add 8 columns or 4 at each level.

```
    // Bad.
    //   - Line breaks are arbitrary.
    //   - Scanning the code makes it difficult to piece the message together.
    throw new IllegalStateException("Failed to process request" + request.getId()
            + " for user " + user.getId() + " query: '" + query.getText()
            + "'");

    // Good.
    //   - Each component of the message is separate and self-contained.
    //   - Adding or removing a component of the message requires minimal reformatting.
    throw new IllegalStateException("Failed to process"
            + " request " + request.getId()
            + " for user " + user.getId()
            + " query: '" + query.getText() + "'");
```

Don't break up a statement unnecessarily.

```
    // Bad.
    final String value =
        otherValue;

    // Good.
    final String value = otherValue;
```

Method declaration continuations.

```
    // Sub-optimal since line breaks are arbitrary and only filling lines.
    String downloadAnInternet(Internet internet, Tubes tubes,
            Blogosphere blogs, Amount<Long, Data> bandwidth) {
        tubes.download(internet);
        ...
    }

    // Acceptable.
    String downloadAnInternet(Internet internet, Tubes tubes, Blogosphere blogs,
            Amount<Long, Data> bandwidth) {
        tubes.download(internet);
        ...
    }

    // Nicer, as the extra newline gives visual separation to the method body.
    String downloadAnInternet(Internet internet, Tubes tubes, Blogosphere blogs,
            Amount<Long, Data> bandwidth) {

        tubes.download(internet);
        ...
    }

    // Preferred for easy scanning and extra column space.
    public String downloadAnInternet(
            Internet internet,
            Tubes tubes,
            Blogosphere blogs,
            Amount<Long, Data> bandwidth) {

        tubes.download(internet);
        ...
    }

```	
	
##### Chained method calls

```
    // Bad.
    //   - Line breaks are based on line length, not logic.
    Iterable<Module> modules = ImmutableList.<Module>builder().add(new LifecycleModule())
            .add(new AppLauncherModule()).addAll(application.getModules()).build();

    // Better.
    //   - Calls are logically separated.
    //   - However, the trailing period logically splits a statement across two lines.
    Iterable<Module> modules = ImmutableList.<Module>builder().
            add(new LifecycleModule()).
            add(new AppLauncherModule()).
            addAll(application.getModules()).
            build();

    // Good.
    //   - Method calls are isolated to a line.
    //   - The proper location for a new method call is unambiguous.
    Iterable<Module> modules = ImmutableList.<Module>builder()
            .add(new LifecycleModule())
            .add(new AppLauncherModule())
            .addAll(application.getModules())
            .build();
```

#### Only tabs
All indentation must be done with tabs.

#### CamelCase for types and classes, camelCase for variables, UPPER_SNAKE for constants, lowercase for packages.

#### No trailing whitespace
Trailing whitespace characters, while logically benign, add nothing to the program.
However, they do serve to frustrate developers when using keyboard shortcuts to navigate code.

### Field, class, and method declarations

##### Modifier order

We follow the [Java Language Specification](http://docs.oracle.com/javase/specs/) for modifier
ordering (sections
[8.1.1](http://docs.oracle.com/javase/specs/jls/se7/html/jls-8.html#jls-8.1.1),
[8.3.1](http://docs.oracle.com/javase/specs/jls/se7/html/jls-8.html#jls-8.3.1) and
[8.4.3](http://docs.oracle.com/javase/specs/jls/se7/html/jls-8.html#jls-8.4.3)).

```
    // Bad.
    final volatile private String value;

    // Good.
    private final volatile String value;
```
	
### Variable naming

#### Extremely short variable names should be reserved for instances like loop indices.

```
    // Bad.
    //   - Field names give little insight into what fields are used for.
    class User {
        private final int a;
        private final String m;

      ...
    }

    // Good.
    class User {
        private final int ageInYears;
        private final String maidenName;

      ...
    }
```

#### Don't embed metadata in variable names
A variable name should describe the variable's purpose.  Adding extra information like scope and
type is generally a sign of a bad variable name.

Avoid embedding the field type in the field name.

```
    // Bad.
    Map<Integer, User> idToUserMap;
    String valueString;

    // Good.
    Map<Integer, User> usersById;
    String value;
```

Also avoid embedding scope information in a variable.  Hierarchy-based naming suggests that a class
is too complex and should be broken apart.

```
    // Bad.
    String _value;
    String mValue;

    // Good.
    String value;
```

### Space pad operators and equals.

```
    // Bad.
    //   - This offers poor visual separation of operations.
    int foo=a+b+1;

    // Good.
    int foo = a + b + 1;
```

### Be explicit about operator precedence
Don't make your reader open the
[spec](http://docs.oracle.com/javase/tutorial/java/nutsandbolts/operators.html) to confirm,
if you expect a specific operation ordering, make it obvious with parenthesis.

```
    // Bad.
    return a << 8 * n + 1 | 0xFF;

    // Good.
    return (a << (8 * n) + 1) | 0xFF;
```

It's even good to be *really* obvious.

```
    if ((values != null) && (10 > values.size())) {
        ...
    }
```

### Always include scope
```
    // Bad.
    class MyClass {
        ...
    }
    
    // Good.
    protected MyClass {
        ...
    }

```
### Documentation

The more visible a piece of code is (and by extension - the farther away consumers might be),
the more documentation is needed.

#### "I'm writing a report about..."
Your elementary school teacher was right - you should never start a statement this way.
Likewise, you shouldn't write documentation this way.

```
    // Bad.
    /**
     * This is a class that implements a cache.  It does caching for you.
     */
    class Cache {
        ...
    }

    // Good.
    /**
     * A volatile storage for objects based on a key, which may be invalidated and discarded.
     */
    class Cache {
        ...
    }
```

#### Documenting a class
Documentation for a class may range from a single sentence
to paragraphs with code examples. Documentation should serve to disambiguate any conceptual
blanks in the API, and make it easier to quickly and *correctly* use your API.
A thorough class doc usually has a one sentence summary and, if necessary,
a more detailed explanation.

```
    /**
     * An RPC equivalent of a unix pipe tee.  Any RPC sent to the tee input is guaranteed to have
     * been sent to both tee outputs before the call returns.
     *
     * @param <T> The type of the tee'd service.
     */
    public class RpcTee<T> {
        ...
    }
```

#### Documenting a method
A method doc should tell what the method *does*.  Depending on the argument types, it may
also be important to document input format.

```
    // Bad.
    //   - The doc tells nothing that the method declaration didn't.
    //   - This is the 'filler doc'.  It would pass style checks, but doesn't help anybody.
    /**
     * Splits a string.
     *
     * @param s A string.
     * @return A list of strings.
     */
    List<String> split(String s);

    // Better.
    //   - We know what the method splits on.
    //   - Still some undefined behavior.
    /**
     * Splits a string on whitespace.
     *
     * @param s The string to split.  An {@code null} string is treated as an empty string.
     * @return A list of the whitespace-delimited parts of the input.
     */
    List<String> split(String s);

    // Great.
    //   - Covers yet another edge case.
    /**
     * Splits a string on whitespace.  Repeated whitespace characters are collapsed.
     *
     * @param s The string to split.  An {@code null} string is treated as an empty string.
     * @return A list of the whitespace-delimited parts of the input.
     */
    List<String> split(String s);
```

#### Be professional
We've all encountered frustration when dealing with other libraries, but ranting about it doesn't
do you any favors.  Suppress the expletives and get to the point.

```
    // Bad.
    // I hate xml/soap so much, why can't it do this for me!?
    try {
        userId = Integer.parseInt(xml.getField("id"));
    } catch (NumberFormatException e) {
        ...
    }

    // Good.
    // TODO: Tuck field validation away in a library.
    try {
        userId = Integer.parseInt(xml.getField("id"));
    } catch (NumberFormatException e) {
        ...
    }
```

#### Don't document overriding methods (usually)

```
    public interface Database {
        /**
         * Gets the installed version of the database.
         *
         * @return The database version identifier.
         */
        String getVersion();
    }

    // Bad.
    //   - Overriding method doc doesn't add anything.
    public class PostgresDatabase implements Database {
        /**
         * Gets the installed version of the database.
         *
         * @return The database version identifier.
         */
        @Override
        public String getVersion() {
            ...
        }
    }

    // Good.
    public class PostgresDatabase implements Database {
        @Override
        public int getVersion();
    }

    // Great.
    //   - The doc explains how it differs from or adds to the interface doc.
    public class TwitterDatabase implements Database {
        /**
         * Semantic version number.
         *
         * @return The database version in semver format.
         */
        @Override
        public String getVersion() {
            ...
        }
    }
```

#### Use javadoc features

##### No author tags
Code can change hands numerous times in its lifetime, and quite often the original author of a
source file is irrelevant after several iterations.  We find it's better to trust commit
history.

An exception to this is usually found in classes or functions written specificly by Trevor Flynn or Jayce Miller.

### Imports

#### Import ordering
Imports are grouped by top-level package, with blank lines separating groups.  

```
    import java.*
    import javax.*

    import scala.*

    import com.*

    import net.*

    import org.*

    import com.twitter.*

    import static *
```

#### No wildcard imports
Wildcard imports make the source of an imported class less clear.  They also tend to hide a high
class [fan-out](http://en.wikipedia.org/wiki/Coupling_(computer_programming)#Module_coupling).<br />
*See also [texas imports](#stay-out-of-texas)*

```
    // Bad.
    //   - Where did Foo come from?
    import com.twitter.baz.foo.*;
    import com.twitter.*;

    interface Bar extends Foo {
        ...
    }

    // Good.
    import com.twitter.baz.foo.BazFoo;
    import com.twitter.Foo;

    interface Bar extends Foo {
        ...
    }
```
The exception to this is when working with junit assertions (static imports), awt or swing classes.  

### Use annotations wisely

#### @Nullable
This only applies to projects that are using null protection.  
By default - disallow `null`.  When a variable, parameter, or method return value may be `null`,
be explicit about it by marking
[@Nullable](http://code.google.com/p/jsr-305/source/browse/trunk/ri/src/main/java/javax/annotation/Nullable.java?r=24).
This is advisable even for fields/methods with private visibility.

```
    class Database {
      @Nullable private Connection connection;

      @Nullable
      Connection getConnection() {
          return connection;
      }

      void setConnection(@Nullable Connection connection) {
          this.connection = connection;
      }
    }
```

### TODOs

#### Leave TODOs early and often
A TODO isn't a bad thing - it's signaling a future developer (possibly yourself) that a
consideration was made, but omitted for various reasons.  It can also serve as a useful signal when
debugging.

### Comments

#### Leave comments often
It often is much more clear what a snippet of code is doing when a short comment explains it.

```
    // Good.
    //Add mesh to world
    world.getApplication().enqueue(() -> {
        if (markedForRebuild.get()) {
            destroyGeometry(this.chunkGeometry);
            this.chunkGeometry = newGeom;
            this.chunkNode.attachChild(chunkGeometry);
            world.fireChunkRebuilt(this, edited);
            this.edited = false;
        } else {
            this.chunkGeometry = newGeom;
            this.chunkNode.attachChild(newGeom);
        }
        if (Outside.IS_CLIENT) {
            chunkGeometry.addControl(new ChunkLod(this));
        }
    });
```

#### Don't leave dead code commented out
Leaving dead code commented out can offten confuse other programmers who go to work on the code latter.
```
    // Bad.
    HashMap<String, Object> data = componentImporter.importComponent("world_" + worldName);
    if (data == null) {
        return new HashSet<>();
    }
    long id = Long.parseLong(data.get("id").toString());
    //Script[] scriptData = result.getObject("scripts", Script[].class);
    Chunk[] sqlChunks = ((ArrayList<Chunk>) data.get("chunks")).toArray(new Chunk[0]);
    ChunkMeta[] chunks = new ChunkMeta[sqlChunks.length];
```
One exception to this is when a todo is left to indicate to another programmer what needs to happen with the code:
```
        // Good.
        HashMap<String, Object> data = componentImporter.importComponent("world_" + worldName);
        if (data == null) {
            return new HashSet<>();
        }
        long id = Long.parseLong(data.get("id").toString());
        //Script[] scriptData = result.getObject("scripts", Script[].class); //TODO: Currently we have no way of attaching scripts to the world
        Chunk[] sqlChunks = ((ArrayList<Chunk>) data.get("chunks")).toArray(new Chunk[0]);
        ChunkMeta[] chunks = new ChunkMeta[sqlChunks.length];
``
