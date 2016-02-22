package com.proudfly.appmanager;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeoutException;

/**
 * Created by LW on 2016/2/22.
 */
public class ShellCommand {
    public static void execCommand(String[] commands, boolean isRoot,
                                   ShellCommandListener listener) throws IOException,
            InterruptedException, TimeoutException {

        int exitCode = -1;
        CommandResult result = null;
        if (commands == null || commands.length == 0) {
            result = new CommandResult(exitCode, null, null);
            listener.onCommandFinished(result);
        }

        Process process = null;
        BufferedReader successReader = null;
        BufferedReader errorReader = null;
        StringBuilder successMsg = null;
        StringBuilder errorMsg = null;

        DataOutputStream os = null;
        process = Runtime.getRuntime().exec(isRoot ? "su" : "sh");
        os = new DataOutputStream(process.getOutputStream());
        for (String command : commands) {
            if (command == null) {
                continue;
            }

            // donnot use os.writeBytes(commmand), avoid chinese charset
            // error
            os.write(command.getBytes());
            os.writeBytes("\n");
            os.flush();
        }
        os.writeBytes("exit\n");
        os.flush();

        exitCode = process.waitFor();
        successMsg = new StringBuilder();
        errorMsg = new StringBuilder();

        successReader = new BufferedReader(new InputStreamReader(
                process.getInputStream()));
        errorReader = new BufferedReader(new InputStreamReader(
                process.getErrorStream()));
        String s = null;
        while ((s = successReader.readLine()) != null) {
            successMsg.append(s + "\n");
        }
        while ((s = errorReader.readLine()) != null) {
            errorMsg.append(s + "\n");
        }

        if (exitCode == -257) {
            throw new TimeoutException();
        }

        try {
            if (os != null) {
                os.close();
            }
            if (successReader != null) {
                successReader.close();
            }
            if (errorReader != null) {
                errorReader.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (process != null) {
            process.destroy();
        }
        result = new CommandResult(exitCode, successMsg == null ? null
                : successMsg.toString(), errorMsg == null ? null
                : errorMsg.toString());

        listener.onCommandFinished(result);
    }

    /**
     * result of command,
     *
     * @author Trinea 2013-5-16
     */
    public static class CommandResult {

        /** result of command **/
        public int exitCode;
        /** success message of command result **/
        public String successMsg;
        /** error message of command result **/
        public String errorMsg;

        public CommandResult(int result) {
            this.exitCode = result;
        }

        public CommandResult(int result, String successMsg, String errorMsg) {
            this.exitCode = result;
            this.successMsg = successMsg;
            this.errorMsg = errorMsg;
        }

        @Override
        public String toString() {
            return "exitCode=" + exitCode + "; successMsg=" + successMsg
                    + "; errorMsg=" + errorMsg;
        }
    }

    public interface ShellCommandListener {
        public void onCommandFinished(CommandResult result);
    }
}
