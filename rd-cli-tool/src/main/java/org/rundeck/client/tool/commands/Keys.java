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

import lombok.Getter;
import lombok.Setter;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import org.rundeck.client.api.model.KeyStorageItem;
import org.rundeck.client.tool.InputError;
import org.rundeck.client.tool.extension.BaseCommand;
import org.rundeck.client.util.Client;
import org.rundeck.client.util.ServiceClient;
import org.rundeck.client.util.Util;
import picocli.CommandLine;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * keys subcommands
 */
@CommandLine.Command(description = "Manage Keys via the Key Storage Facility." +
        "\nSpecify the path using -p/--path, or as the last argument to the command.", name = "keys")

public class Keys extends BaseCommand {

    public static final String P_PATH_IS_REQUIRED = "-p/--path is required";

    public static class Path {
        final String pathString;

        public Path(final String pathString) {
            this.pathString = pathString;
        }

        public String keysPath() {
            if (pathString.startsWith("keys/")) {
                return pathString.substring(5);
            }
            return pathString;
        }

        @Override
        public String toString() {
            return pathString;
        }
    }


    @CommandLine.Option(names = {"-p", "--path"},
            description = "Storage path in the form 'path/to/file', or 'keys/path/to/file'.",
            defaultValue = "")
    @Getter
    @Setter
    private Path path;

    @CommandLine.Spec
    CommandLine.Model.CommandSpec spec;

    void validateRequired() {
        String path1 = path.keysPath();
        if (path1.length() < 1) {
            throw new CommandLine.ParameterException(spec.commandLine(), P_PATH_IS_REQUIRED);
        }
    }

    @CommandLine.Command(description = "List the keys and directories at a given path, or at the root by default.",
            aliases = {"ls"})
    public boolean list() throws IOException, InputError {

        KeyStorageItem keyStorageItem = apiCall(api -> api.listKeyStorage(path.keysPath()));

        getRdOutput().output(keyStorageItem.toBasicString());
        if (keyStorageItem.getType() == KeyStorageItem.KeyItemType.directory) {
            getRdOutput().output(
                    keyStorageItem.getResources()
                            .stream()
                            .sorted()
                            .map(KeyStorageItem::toBasicString)
                            .collect(Collectors.toList()));
            return true;
        } else {
            getRdOutput().error(String.format("Path is not a directory: %s", path));
            return false;
        }
    }


    @CommandLine.Command(description = "Get metadata about the given path")
    public void info() throws IOException, InputError {

        KeyStorageItem keyStorageItem = apiCall(api -> api.listKeyStorage(keysPath(path.keysPath())));

        getRdOutput().output(String.format("Path: %s", keyStorageItem.getPath()));
        getRdOutput().output(String.format("Type: %s", keyStorageItem.getType()));

        if (keyStorageItem.getType() == KeyStorageItem.KeyItemType.directory) {
            getRdOutput().output(String.format("Directory: %d entries", keyStorageItem.getResources().size()));
        } else {
            getRdOutput().output(String.format("Name: %s", keyStorageItem.getName()));
            getRdOutput().output("Metadata:");
            getRdOutput().output(keyStorageItem.getMetaString("  "));
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

    @Getter @Setter
    static class GetOpts {

        @CommandLine.Option(names = {"-f", "--file"},
                description = "File path for storing the public key. If unset, the output will be written to stdout.")
        private File file;
    }

    @CommandLine.Command(description = "Get the contents of a public key")
    public boolean get(@CommandLine.Mixin GetOpts options) throws IOException, InputError {
        validateRequired();
        KeyStorageItem keyStorageItem = apiCall(api -> api.listKeyStorage(path.keysPath()));

        if (keyStorageItem.getType() != KeyStorageItem.KeyItemType.file) {
            getRdOutput().error(String.format("Requested path (%s) is not a file", path));
            return false;
        }
        if (keyStorageItem.getFileType() != KeyStorageItem.KeyFileType.publicKey) {
            getRdOutput().error(String.format(
                    "Requested path (%s) is not a public key. Type: %s",
                    path,
                    keyStorageItem.getFileType()
            ));
            return false;
        }
        try (ResponseBody body = apiCall(api -> api.getPublicKey(path.keysPath()))) {
            if (!ServiceClient.hasAnyMediaType(body.contentType(), Client.MEDIA_TYPE_GPG_KEYS)) {
                throw new IllegalStateException("Unexpected response format: " + body.contentType());
            }
            InputStream inputStream = body.byteStream();
            File outFile = options.getFile();
            if (outFile != null) {
                try (FileOutputStream out = new FileOutputStream(outFile)) {
                    long total = Util.copyStream(inputStream, out);
                    getRdOutput().info(String.format(
                            "Wrote %d bytes of %s to file %s%n",
                            total,
                            body.contentType(),
                            outFile
                    ));
                }
            } else {
                Util.copyStream(inputStream, System.out);
            }
        }
        return true;
    }


    @CommandLine.Command(aliases = {"rm"}, description = "Delete the key at the given path.")
    public void delete() throws IOException, InputError {
        validateRequired();
        apiCall(api -> api.deleteKeyStorage(path.keysPath()));
        getRdOutput().info(String.format("Deleted: %s", path));
    }

    @Getter @Setter
    static class Upload {


        @CommandLine.Option(names = {"-t", "--type"},
                description = "Type of key to store: publicKey,privateKey,password.")
        private KeyStorageItem.KeyFileType type;

        @CommandLine.Option(names = {"-f", "--file"},
                description = "File path for reading the upload contents.")
        private File file;

        boolean isFile() {
            return file != null;
        }

        @CommandLine.Option(
                names = {"--charset"},
                description = "Encoding charset of the File, e.g. 'UTF-8'. If not specified, the JVM default will be " +
                        "used.")
        private String charset;

        boolean isCharset() {
            return charset != null;
        }

        @CommandLine.Option(
                names = {"-P", "--prompt"},
                description = "(password type only) prompt on console for the password value, if -f is not specified."
        )
        private boolean prompt;
    }

    @CommandLine.Command(description = "Create a new key entry.")
    public void create(@CommandLine.Mixin Upload options) throws IOException, InputError {
        validateRequired();
        RequestBody requestBody = prepareKeyUpload(options);


        KeyStorageItem keyStorageItem = apiCall(api -> api.createKeyStorage(path.keysPath(), requestBody));
        getRdOutput().info(String.format("Created: %s", keyStorageItem.toBasicString()));
    }

    static RequestBody prepareKeyUpload(final Upload options) throws IOException, InputError {
        MediaType contentType = getUploadContentType(options.getType());
        if (null == contentType) {
            throw new InputError(String.format("Type is not supported: %s", options.getType()));
        }
        RequestBody requestBody;
        if (options.getType() != KeyStorageItem.KeyFileType.password && !options.isFile()) {
            throw new InputError(String.format("File (-f/--file) is required for type: %s", options.getType()));
        }
        if (options.getType() == KeyStorageItem.KeyFileType.password && !options.isFile() && !options.isPrompt()) {
            throw new InputError(String.format(
                    "File (-f/--file) or -P/--prompt is required for type: %s",
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
                CharBuffer buffer = CharBuffer.allocate((int) input.length());
                buffer.mark();
                try (
                        InputStreamReader read = new InputStreamReader(
                                Files.newInputStream(input.toPath()),
                                options.isCharset() ? Charset.forName(options.getCharset()) : Charset.defaultCharset()
                        )
                ) {
                    int len = read.read(buffer);
                    while (len > 0) {
                        len = read.read(buffer);
                    }
                }
                buffer.reset();
                //locate first newline char
                int limit = 0;
                for (; limit < buffer.length(); limit++) {
                    char c = buffer.charAt(limit);
                    if (c == '\r' || c == '\n') {
                        break;
                    }
                }
                buffer.limit(limit);
                if (buffer.length() == 0) {
                    throw new IllegalStateException("No content found in file: " + input);
                }

                ByteBuffer byteBuffer = StandardCharsets.UTF_8.encode(buffer);
                requestBody = RequestBody.create(
                        Arrays.copyOfRange(byteBuffer.array(), byteBuffer.position(), byteBuffer.limit()),
                        contentType
                );
            } else {
                requestBody = RequestBody.create(
                        input,
                        contentType
                );
            }
        } else {
            char[] chars = System.console().readPassword("Enter password: ");
            ByteBuffer byteBuffer = StandardCharsets.UTF_8.encode(CharBuffer.wrap(chars));
            requestBody = RequestBody.create(
                    Arrays.copyOfRange(byteBuffer.array(), byteBuffer.position(), byteBuffer.limit()),
                    contentType
            );
        }
        return requestBody;
    }

    @CommandLine.Command(description = "Update an existing key entry")
    public void update(@CommandLine.Mixin Upload options) throws IOException, InputError {
        validateRequired();
        RequestBody requestBody = prepareKeyUpload(options);
        KeyStorageItem keyStorageItem = apiCall(api -> api.updateKeyStorage(path.keysPath(), requestBody));
        getRdOutput().info(String.format("Updated: %s", keyStorageItem.toBasicString()));
    }

    private static MediaType getUploadContentType(final KeyStorageItem.KeyFileType type) {
        switch (type) {
            case privateKey:
                return Client.MEDIA_TYPE_OCTET_STREAM;
            case publicKey:
                return Client.MEDIA_TYPE_GPG_KEYS;
            case password:
                return Client.MEDIA_TYPE_X_RUNDECK_PASSWORD;
            default:
                return null;
        }
    }

}
