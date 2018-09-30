package launchit.auth.model;

import com.google.gson.annotations.Expose;

public class YggdrasilError extends Throwable
{
    @Expose
    private String error;

    @Expose
    private String errorMessage;

    @Override
    public String getMessage() {
        return this.error + ": " + this.errorMessage;
    }

    public String getError() {
        return error;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}