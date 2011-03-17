package views;

import glazedlists.matchers.ColourMatcher;

import java.util.ArrayList;
import java.util.List;

import listeners.ToleranceChangeListener;
import net.ffxml.swtforms.builder.ButtonBarBuilder;
import net.ffxml.swtforms.extras.DefaultFormBuilder;
import net.ffxml.swtforms.layout.FormLayout;

import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Shell;

public class ColourMapPropertiesDialog extends ApplicationWindow {
    private Scale tolScale;
    private List<ToleranceChangeListener> listeners = new ArrayList<ToleranceChangeListener>();
    private int tolerance;
    private Button closeButton;
    private Button defaultButton;

    public ColourMapPropertiesDialog(Shell parent, int initialTolerance) {
        super(parent);
        this.tolerance = initialTolerance;
        setShellStyle(SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
    }

    @Override
    protected Control createContents(Composite parent) {
        getShell().setText("Properties");

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(1, true));
        Composite formComposite = new Composite(composite, SWT.NONE);
        Composite buttonComposite = new Composite(composite, SWT.NONE);

        FormLayout layout = new FormLayout(
                "right:max(40dlu;pref), 10dlu, 200dlu", "");

        DefaultFormBuilder builder = new DefaultFormBuilder(formComposite,
                layout);
        builder.setDefaultDialogBorder();

        Label tolLabel;
        builder.append(tolLabel = new Label(formComposite, SWT.NONE),
                tolScale = new Scale(formComposite, SWT.HORIZONTAL));
        tolLabel.setText("Colour Tolerance");

        tolLabel
                .setToolTipText("Slide this to change how similar colours need to be to your selections");
        tolScale.setMinimum(10);
        tolScale.setIncrement(5);
        tolScale.setMaximum(100);
        tolScale.setSelection(tolerance);

        ButtonBarBuilder butBarBuilder = new ButtonBarBuilder(buttonComposite);

        butBarBuilder.addGriddedButtons(new Button[] {
                defaultButton = closeButton = new Button(buttonComposite,
                        SWT.NONE),
                closeButton = new Button(buttonComposite, SWT.NONE) });

        defaultButton.setText("&Default");
        closeButton.setText("&Close");
        closeButton.setFocus();

        initListeners();

        return composite;
    }

    private void initListeners() {
        // fire event when slider changed
        tolScale.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                fireToleranceChanged();
            }
        });

        defaultButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                setTolerance(ColourMatcher.DEFAULT_TOLERANCE);
            };
        });

        closeButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                close();
            };
        });

    }

    public void setTolerance(int tolerance) {
        if (tolScale == null) {
            throw new IllegalStateException("Widgets not created yet");
        }
        this.tolerance = tolerance;
        tolScale.setSelection(tolerance);
        fireToleranceChanged();
    }

    public void addToleranceChangeListener(ToleranceChangeListener l) {
        listeners.add(l);
    }

    private void fireToleranceChanged() {
        tolScale.setToolTipText(tolScale.getSelection() + "/"
                + tolScale.getMaximum());
        for (ToleranceChangeListener l : listeners) {
            l.toleranceChanged(tolScale.getSelection());
        }
    }
}
