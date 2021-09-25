// From https://github.com/sgjesse/r8api/tree/0f41b95db680ddc98fd7197f31a800f4ed56816b/
// r8api/src/main/java/dk/sgjesse/r8api/FileOrigin.java

package dk.sgjesse.r8api;

import com.android.tools.r8.origin.Origin;

import java.io.File;
import java.nio.file.Path;

/**
 * File component in a D8/R8 origin description.
 */
public class FileOrigin extends Origin {

    private final File file;

    public FileOrigin(File file) {
        super(root());
        assert file != null;
        this.file = file;
    }

    @Override
    public String part() {
        return file.toString();
    }
}
