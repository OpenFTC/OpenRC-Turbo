package dk.sgjesse.d8onandroid;

import com.android.tools.r8.Diagnostic;
import com.android.tools.r8.DiagnosticsHandler;

public class D8DiagnosticsHandler implements DiagnosticsHandler {
    @Override
    public void error(Diagnostic diagnostic) {
        System.out.println("ERROR: " + diagnostic.getDiagnosticMessage());
    }
    @Override
    public void warning(Diagnostic diagnostic) {
        System.out.println("WARNING: " + diagnostic.getDiagnosticMessage());
    }

    @Override
    public void info(Diagnostic diagnostic) {
        System.out.println("INFO: " + diagnostic.getDiagnosticMessage());
    }
}
