package auroracafe.network;
import java.io.Serializable;
import java.nio.file.Path;
public class MenuCsvRequest implements Serializable {
    public final Path path;
    public final boolean replaceAll;
    public MenuCsvRequest(Path path, boolean replaceAll) { this.path = path; this.replaceAll = replaceAll; }
}
