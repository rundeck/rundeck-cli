package org.rundeck.client.tool.extension;

import lombok.Getter;
import lombok.Setter;
import org.rundeck.client.api.RundeckApi;
import org.rundeck.client.tool.CommandOutput;
import org.rundeck.client.tool.InputError;
import retrofit2.Call;

import java.io.IOException;
import java.util.function.Function;

public class BaseCommand implements RdCommandExtension, RdOutput {

    @Setter
    @Getter
    private RdTool rdTool;
    @Setter
    @Getter
    private CommandOutput rdOutput;


    public <T> T apiCall(Function<RundeckApi, Call<T>> func) throws InputError, IOException {
        return rdTool.apiCall(func);
    }
}
