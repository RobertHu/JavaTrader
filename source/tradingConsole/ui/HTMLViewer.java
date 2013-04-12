package tradingConsole.ui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

public class HTMLViewer extends JPanel
{
    private JScrollPane _scrollPane;
    private JEditorPane _htmlBrowser;

    //private JPanel _statusPanel;
    //private JButton _firstButton;
    //private JButton _lastButton;
    //private JButton _previousButton;
    //private JButton _nextButton;
    //private JLabel _status;

    private ArrayList<URL> _urlHistory = new ArrayList<URL>();
    private URL _currentURL = null;

    public HTMLViewer()
    {
        this.setLayout(new GridBagLayout());
        this._htmlBrowser = new JEditorPane();
        this._scrollPane = new JScrollPane(this._htmlBrowser);

        //this._statusPanel = new JPanel(new GridBagLayout());
        //this._statusPanel.setBorder(BorderFactory.createEtchedBorder());

        /*this._firstButton = new JButton("|<");
        ActionListener actionListener = new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                setUrl(getFirstPage());
            }
        };
        this._firstButton.addActionListener(actionListener);

        this._lastButton = new JButton(">|");
        actionListener = new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                setUrl(getLastPage());
            }
        };
        this._lastButton.addActionListener(actionListener);*/

        /*this._previousButton = new JButton(this.getIcon("previous.ico"));
        //this._previousButton.setIcon(this.getIcon("previous.ico"));
        ActionListener actionListener = new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                setUrl(getPreviousPage());
            }
        };
        this._previousButton.addActionListener(actionListener);

        this._nextButton = new JButton(this.getIcon("next.ico"));
        //this._nextButton.setIcon(this.getIcon("next.ico"));
        actionListener = new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                setUrl(getNextPage());
            }
        };
        this._nextButton.addActionListener(actionListener);

        this._status = new JLabel();
        this._status.setHorizontalAlignment(SwingConstants.RIGHT);

        //this._statusPanel.add(this._firstButton, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
		//	, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 2, 0, 0), 0, 0));
        this._statusPanel.add(this._previousButton, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 2, 0, 0), 0, 0));
        this._statusPanel.add(this._nextButton, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 2, 0, 0), 0, 0));
        //this._statusPanel.add(this._lastButton, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0
		//	, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 2, 0, 0), 0, 0));
        this._statusPanel.add(this._status, new GridBagConstraints(2, 0, 1, 1, 1.0, 0.0
			, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, new Insets(2, 0, 0, 0), 0, 0));

        this.add(this._statusPanel, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 20), 0, 4));*/
        this.add(this._scrollPane, new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0
			, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

        this._htmlBrowser.setEditable(false);
        HyperlinkListener hyperlinkListener = new HyperlinkListener()
        {
            public void hyperlinkUpdate(HyperlinkEvent e)
            {
                if(e.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
                {
                    setUrl(e.getURL());
                }
                else if(e.getEventType() == HyperlinkEvent.EventType.ENTERED)
                {
                    //_status.setText("Ready");
                    //_htmlBrowser.setEnabled(true);
                }
            }
        };
        this._htmlBrowser.addHyperlinkListener(hyperlinkListener);
    }

    public void setUrl(URL url)
    {
        if(url != null)
        {
            try
            {
                //this._status.setText("Loading...");
                //this._htmlBrowser.setEnabled(false);
                this._htmlBrowser.setPage(url);

                this._currentURL = url;
                if (!this._urlHistory.contains(url))
                {
                    this._urlHistory.add(url);
                }
                this.updateOperateStatus();
            }
            catch (IOException ex)
            {
                //this._status.setText("Can't access " + url);
                //_htmlBrowser.setEnabled(true);
            }
        }
    }

    private void updateOperateStatus()
    {
        int index = this._urlHistory.indexOf(this._currentURL);
        int size = this._urlHistory.size();

        /*this._firstButton.setEnabled(size > 0);
        this._lastButton.setEnabled(size > 0);*/

        //this._previousButton.setEnabled(index >= 0);
        //this._nextButton.setEnabled(index >= 0);

        if(index == 0)
        {
            //this._firstButton.setEnabled(false);
            //this._previousButton.setEnabled(false);
        }

        if(index == size - 1)
        {
            //this._lastButton.setEnabled(false);
            //this._nextButton.setEnabled(false);
        }
    }

    /*private URL getFirstPage()
    {
        return this._urlHistory.get(0);
    }

    private URL getLastPage()
    {
        return this._urlHistory.get(this._urlHistory.size() - 1);
    }*/

    private URL getPreviousPage()
    {
        int index = this._urlHistory.indexOf(this._currentURL);
        return this._urlHistory.get(index - 1);
    }

    private URL getNextPage()
    {
        int index = this._urlHistory.indexOf(this._currentURL);
        return this._urlHistory.get(index + 1);
    }

    /*private Icon getIcon(String fileName)
    {
        String fullFileName = "D:\\Temp\\AutoUpdater\\src\\Images\\" + fileName;
        return new ImageIcon(fullFileName);
    }*/
}
