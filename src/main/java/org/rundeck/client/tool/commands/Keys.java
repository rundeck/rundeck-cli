package org.rundeck.client.tool.commands;

import com.lexicalscope.jewel.cli.CommandLineInterface;
import com.lexicalscope.jewel.cli.Option;
import com.lexicalscope.jewel.cli.Unparsed;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import org.rundeck.client.api.RundeckApi;
import org.rundeck.client.api.model.KeyStorageItem;
import org.rundeck.client.util.Client;
import org.rundeck.client.util.Util;
import org.rundeck.util.toolbelt.Command;
import org.rundeck.util.toolbelt.CommandOutput;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by greg on 6/7/16.
 */
@Command(description = "Manage Keys via the Key Storage Facility." +
                       "\nSpecify the path using -p/--path, or as the last argument to the command.")

public class Keys extends ApiCommand {
    public Keys(final Client<RundeckApi> client) {
        super(client);
    }

    public static class Path {
        String path;

        public Path(final String path) {
            this.path = path;
        }

        public String keysPath() {
            if (path.startsWith("keys/")) {
                return path.substring(5);
            }
            return path;
        }

        @Override
        public String toString() {
            return path;
        }
    }

    static interface PathArgs {

        @Option(shortName = "p",
                longName = "path",
                description = "Storage path, default: keys/",
                defaultValue = "keys/")
        Path getPath();

        @Unparsed(defaultToNull = true, description = "Storage path", name = "PATH")
        Path getPath2();
    }

    @CommandLineInterface(application = "list")
    static interface ListArg extends PathArgs {
    }

    @Command(description = "List the keys and directories at a given path, or at the root by default.",
             synonyms = {"ls"})
    public boolean list(ListArg options, CommandOutput output) throws IOException {

        Path path = argPath(options);
        KeyStorageItem keyStorageItem = client.checkError(client.getService()
                                                                .listKeyStorage(path.keysPath()));

        output.output(keyStorageItem.toBasicString());
        if (keyStorageItem.getType() == KeyStorageItem.KeyItemType.directory) {
            keyStorageItem.getResources()
                          .stream()
                          .sorted()
                          .forEach(i -> output.output("  " + i.toBasicString()));
            return true;
        } else {
            output.error(String.format("Path is not a directory: %s", path));
            return false;
        }
    }

    private Path argPath(final PathArgs options) {
        return options.getPath2() != null ? options.getPath2() : options.getPath();
    }

    @CommandLineInterface(application = "info")
    static interface Info extends PathArgs {
    }

    @Command(description = "Get metadata about the given path")
    public void info(Info options, CommandOutput output) throws IOException {
        Path path = argPath(options);
        KeyStorageItem keyStorageItem = client.checkError(client.getService()
                                                                .listKeyStorage(keysPath(path
                                                                                                 .keysPath())));

        output.output(String.format("Path: %s", keyStorageItem.getPath()));
        output.output(String.format("Type: %s", keyStorageItem.getType()));
//        output.output(keyStorageItem.toBasicString());
        if (keyStorageItem.getType() == KeyStorageItem.KeyItemType.directory) {
            output.output(String.format("Directory: %d entries", keyStorageItem.getResources().size()));
        } else {
            output.output(String.format("Name: %s", keyStorageItem.getName()));
            output.output("Metadata:");
            output.output(keyStorageItem.getMetaString("  "));
        }
    }

    /**
     * Remove keys/ prefix if present
     *
     * @param path
     *
     * @return
     */
    private String keysPath(final String path) {
        if (path.startsWith("keys/")) {
            return path.substring(5);
        }
        return path;
    }

    @CommandLineInterface(application = "get")
    static interface GetOpts extends PathArgs {

        @Option(shortName = "f",
                longName = "file",
                defaultToNull = true,
                description = "File path for storing the public key. If unset, the output will be written to stdout.")
        File getFile();
    }

    @Command(description = "Get the contents of a public key")
    public boolean get(GetOpts options, CommandOutput output) throws IOException {
        Path path = argPath(options);
        KeyStorageItem keyStorageItem = client.checkError(client.getService()
                                                                .listKeyStorage(path.keysPath()));

        if (keyStorageItem.getType() != KeyStorageItem.KeyItemType.file) {
            output.error(String.format("Requested path (%s) is not a file", path));
            return false;
        }
        if (keyStorageItem.getFileType() != KeyStorageItem.KeyFileType.publicKey) {
            output.error(String.format(
                    "Requested path (%s) is not a public key. Type: %s",
                    path,
                    keyStorageItem.getFileType()
            ));
            return false;
        }
        ResponseBody body = client.checkError(client.getService().getPublicKey(path.keysPath()));
        if (!Client.hasAnyMediaType(body, Client.MEDIA_TYPE_GPG_KEYS)) {
            throw new IllegalStateException("Unexpected response format: " + body.contentType());
        }
        InputStream inputStream = body.byteStream();
        File outFile = options.getFile();
        if (outFile != null) {
            try (FileOutputStream out = new FileOutputStream(outFile)) {
                long total = Util.copyStream(inputStream, out);
                output.output(String.format(
                        "Wrote %d bytes of %s to file %s%n",
                        total,
                        body.contentType(),
                        outFile
                ));
            }
        } else {
            long total = Util.copyStream(inputStream, System.out);
        }
        return true;
    }

    @CommandLineInterface(application = "delete") static interface Delete extends PathArgs {

    }

    @Command(synonyms = {"rm"}, description = "Delete the key at the given path.")
    public void delete(Delete opts, CommandOutput output) throws IOException {
        Path path = argPath(opts);
        client.checkError(client.getService().deleteKeyStorage(path.keysPath()));
        output.output(String.format("Deleted: %s", path));
    }

    @CommandLineInterface(application = "create")
    static interface Upload extends PathArgs {


        @Option(shortName = "t",
                longName = "type",
                description = "Type of key to store: publicKey,privateKey,password.")
        KeyStorageItem.KeyFileType getType();

        @Option(shortName = "f",
                longName = "file",
                description = "File path for reading the upload contents.")
        File getFile();
    }

    @Command(description = "Create a new key entry")
    public boolean create(Upload options, CommandOutput output) throws IOException {

        File input = options.getFile();
        if (!input.canRead() || !input.isFile()) {
            throw new IllegalArgumentException(String.format("File is not readable or does not exist: %s", input));
        }
        MediaType contentType = getUploadContentType(options.getType());
        if (null == contentType) {
            throw new IllegalArgumentException(String.format("Type is not supported: %s", options.getType()));
        }
        RequestBody requestBody = RequestBody.create(
                contentType,
                input
        );
        Path path = argPath(options);
        KeyStorageItem keyStorageItem = client.checkError(client.getService()
                                                                .createKeyStorage(
                                                                        path.keysPath(),
                                                                        requestBody
                                                                ));
        output.output(String.format("Created: %s", keyStorageItem.toBasicString()));
        return true;
    }

    @CommandLineInterface(application = "update")
    static interface Update extends Upload {
    }

    @Command(description = "Update an existing key entry")
    public boolean update(Update options, CommandOutput output) throws IOException {

        File input = options.getFile();
        if (!input.canRead() || !input.isFile()) {
            throw new IllegalArgumentException(String.format("File is not readable or does not exist: %s", input));
        }
        MediaType contentType = getUploadContentType(options.getType());
        if (null == contentType) {
            throw new IllegalArgumentException(String.format("Type is not supported: %s", options.getType()));
        }
        RequestBody requestBody = RequestBody.create(
                contentType,
                input
        );
        Path path = argPath(options);
        KeyStorageItem keyStorageItem = client.checkError(client.getService()
                                                                .updateKeyStorage(
                                                                        path.keysPath(),
                                                                        requestBody
                                                                ));
        output.output(String.format("Updated: %s", keyStorageItem.toBasicString()));
        return true;
    }

    private MediaType getUploadContentType(final KeyStorageItem.KeyFileType type) {
        return type == KeyStorageItem.KeyFileType.privateKey ? Client.MEDIA_TYPE_OCTET_STREAM :
               type == KeyStorageItem.KeyFileType.publicKey ? Client.MEDIA_TYPE_GPG_KEYS :
               type == KeyStorageItem.KeyFileType.password ? Client.MEDIA_TYPE_X_RUNDECK_PASSWORD : null;
    }

}
