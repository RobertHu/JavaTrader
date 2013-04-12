package tradingConsole.ui;

import java.awt.Insets;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class GridBagConstraints2 extends java.awt.GridBagConstraints
{
	//This constructor is copy from jdk1.2
	public GridBagConstraints2(int gridx, int gridy, int gridwidth,
							  int gridheight, double weightx,
							  double weighty,
							  int anchor, int fill, Insets insets,
							  int ipadx, int ipady)
	{
		super.gridx = gridx;
		super.gridy = gridy;
		super.gridwidth = gridwidth;
		super.gridheight = gridheight;
		super.weightx = weightx;
		super.weighty = weighty;
		super.anchor = anchor;
		super.fill = fill;
		super.insets = insets;
		super.ipadx = ipadx;
		super.ipady = ipady;
	}
}
