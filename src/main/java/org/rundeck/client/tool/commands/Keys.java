/*
 * Copyright 2017 Rundeck, Inc. (http://rundeck.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.rundeck.client.tool.commands;

import com.lexicalscope.jewel.cli.CommandLineInterface;
import com.lexicalscope.jewel.cli.Option;
import com.lexicalscope.jewel.cli.Unparsed;
import com.simplifyops.toolbelt.Command;
import com.simplifyops.toolbelt.CommandOutput;
import com.simplifyops.toolbelt.InputError;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import org.rundeck.client.api.model.KeyStorageItem;
import org.rundeck.client.tool.RdApp;
import org.rundeck.client.util.Client;
import org.rundeck.client.util.Util;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * keys subcommands
 */
@Command(description = "Manage Keys via the Key Storage Facility." +
                       "\nSpecify the path using -p/--path, or as the last argument to the command.")

public class Keys extends AppCommand {
    public Keys(final RdApp client) {
        super(client);
    }

    public static class Path {
        final String path;

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

    interface PathArgs {

        @Option(shortName = "p",
                longName = "path",
                description = "Storage path, default: keys/",
                defaultValue = "keys/")
        Path getPath();

        @Unparsed(defaultToNull = true, description = "Storage path", name = "PATH")
        Path getPath2();
    }

    @CommandLineInterface(application = "list") interface ListArg extends PathArgs {
    }

    @Command(description = "List the keys and directories at a given path, or at the root by default.",
             synonyms = {"ls"})
    public boolean list(ListArg options, CommandOutput output) throws IOException, InputError {

        Path path = argPath(options);
        KeyStorageItem keyStorageItem = apiCall(api -> api.listKeyStorage(path.keysPath()));

        output.output(keyStorageItem.toBasicString());
        if (keyStorageItem.getType() == KeyStorageItem.KeyItemType.directory) {
            output.output(
                    keyStorageItem.getResources()
                                  .stream()
                                  .sorted()
                                  .map(KeyStorageItem::toBasicString)
                                  .collect(Collectors.toList()));
            return true;
        } else {
            output.error(String.format("Path is not a directory: %s", path));
            return false;
        }
    }

    private Path argPath(final PathArgs options) {
        return options.getPath2() != null ? options.getPath2() : options.getPath();
    }

    @CommandLineInterface(application = "info") interface Info extends PathArgs {
    }

    @Command(description = "Get metadata about the given path")
    public void info(Info options, CommandOutput output) throws IOException, InputError {
        Path path = argPath(options);
        KeyStorageItem keyStorageItem = apiCall(api -> api.listKeyStorage(keysPath(path.keysPath())));

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
     */
    private String keysPath(final String path) {
        if (path.startsWith("keys/")) {
            return path.substring(5);
        }
        return path;
    }

    @CommandLineInterface(application = "get") interface GetOpts extends PathArgs {

        @Option(shortName = "f",
                longName = "file",
                defaultToNull = true,
                description = "File path for storing the public key. If unset, the output will be written to stdout.")
        File getFile();
    }

    @Command(description = "Get the contents of a public key")
    public boolean get(GetOpts options, CommandOutput output) throws IOException, InputError {
        Path path = argPath(options);
        String path1 = path.keysPath();
        if (path1.length() < 1) {
            throw new InputError("-p/--path is required");
        }
        KeyStorageItem keyStorageItem = apiCall(api -> api.listKeyStorage(path.keysPath()));

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
        ResponseBody body = apiCall(api -> api.getPublicKey(path.keysPath()));
        if (!Client.hasAnyMediaType(body, Client.MEDIA_TYPE_GPG_KEYS)) {
            throw new IllegalStateException("Unexpected response format: " + body.contentType());
        }
        InputStream inputStream = body.byteStream();
        File outFile = options.getFile();
        if (outFile != null) {
            try (FileOutputStream out = new FileOutputStream(outFile)) {
                long total = Util.copyStream(inputStream, out);
                output.info(String.format(
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

    @CommandLineInterface(application = "delete") interface Delete extends PathArgs {

    }

    @Command(synonyms = {"rm"}, description = "Delete the key at the given path.")
    public void delete(Delete opts, CommandOutput output) throws IOException, InputError {
        Path path = argPath(opts);
        String path1 = path.keysPath();
        if (path1.length() < 1) {
            throw new InputError("-p/--path is required");
        }
        apiCall(api -> api.deleteKeyStorage(path.keysPath()));
        output.info(String.format("Deleted: %s", path));
    }

    @CommandLineInterface(application = "create") interface Upload extends PathArgs {


        @Option(shortName = "t",
                longName = "type",
                description = "Type of key to store: publicKey,privateKey,password.")
        KeyStorageItem.KeyFileType getType();

        @Option(shortName = "f",
                longName = "file",
                description = "File path for reading the upload contents.")
        File getFile();

        boolean isFile();

        @Option(
                shortName = "P",
                longName = "prompt",
                description = "(password type only) prompt on console for the password value, if -f is not specified."
        )
        boolean isPrompt();
    }

    @Command(description = "Create a new key entry.")
    public void create(Upload options, CommandOutput output) throws IOException, InputError {

        Path path = argPath(options);
        String path1 = path.keysPath();
        if (path1.length() < 1) {
            throw new InputError("-p/--path is required");
        }
        RequestBody requestBody = prepareKeyUpload(options);


        KeyStorageItem keyStorageItem = apiCall(api -> api.createKeyStorage(path1, requestBody));
        output.info(String.format("Created: %s", keyStorageItem.toBasicString()));
    }

    static RequestBody prepareKeyUpload(final Upload options) throws IOException, InputError {
        MediaType contentType = getUploadContentType(options.getType());
        if (null == contentType) {
            throw new InputError(String.format("Type is not supported: %s", options.getType()));
        }
        RequestBody requestBody;
        if (options.getType() != KeyStorageItem.KeyFileType.password && !options.isFile()) {
            throw new InputError(String.format("File (-f) is required for type: %s", options.getType()));
        }
        if (options.getType() == KeyStorageItem.KeyFileType.password && !options.isFile() && !options.isPrompt()) {
            throw new InputError(String.format(
                    "File (-f) or -p is required for type: %s",
                    options.getType()
            ));
        }
        if (options.isFile()) {
            File input = options.getFile();
            if (!input.canRead() || !input.isFile()) {
                throw new InputError(String.format("File is not readable or does not exist: %s", input));
            }
            if (options.getType() == KeyStorageItem.KeyFileType.password) {
                //read the first line of the file only, and leave off line breaks
                char[] chars = null;
                try (BufferedReader read = new BufferedReader(new InputStreamReader(new FileInputStream(input)))) {
                    String s = read.readLine();
                    if (null != s) {
                        chars = s.toCharArray();
                    }
                }
                if (chars == null || chars.length == 0) {
                    throw new IllegalStateException("Could not read first line of file: " + input);
                }

                ByteBuffer byteBuffer = Charset.forName("UTF-8").encode(CharBuffer.wrap(chars));
                requestBody = RequestBody.create(
                        contentType,
                        Arrays.copyOfRange(byteBuffer.array(), byteBuffer.position(), byteBuffer.limit())
                );
            } else {
                requestBody = RequestBody.create(
                        contentType,
                        input
                );
            }
        } else {
            char[] chars = System.console().readPassword("Enter password: ");
            ByteBuffer byteBuffer = Charset.forName("UTF-8").encode(CharBuffer.wrap(chars));
            requestBody = RequestBody.create(
                    contentType,
                    Arrays.copyOfRange(byteBuffer.array(), byteBuffer.position(), byteBuffer.limit())
            );
        }
        return requestBody;
    }

    @CommandLineInterface(application = "update") interface Update extends Upload {
    }

    @Command(description = "Update an existing key entry")
    public void update(Update options, CommandOutput output) throws IOException, InputError {
        Path path = argPath(options);
        String path1 = path.keysPath();
        if (path1.length() < 1) {
            throw new InputError("-p/--path is required");
        }
        RequestBody requestBody = prepareKeyUpload(options);
        KeyStorageItem keyStorageItem = apiCall(api -> api.updateKeyStorage(path.keysPath(), requestBody));
        output.info(String.format("Updated: %s", keyStorageItem.toBasicString()));
    }

    static private MediaType getUploadContentType(final KeyStorageItem.KeyFileType type) {
        return type == KeyStorageItem.KeyFileType.privateKey ? Client.MEDIA_TYPE_OCTET_STREAM :
               type == KeyStorageItem.KeyFileType.publicKey ? Client.MEDIA_TYPE_GPG_KEYS :
               type == KeyStorageItem.KeyFileType.password ? Client.MEDIA_TYPE_X_RUNDECK_PASSWORD : null;
    }

}
