package tradingConsole.settings;

import java.util.*;

import framework.*;
import framework.data.*;
import framework.lang.Enum;
import framework.xml.*;
import tradingConsole.enumDefine.*;
import tradingConsole.DealingPolicyDetail;
import tradingConsole.Instrument;

public class VolumeNecessary
{
	private Guid _id;
	private VolumeNecessaryOption _option;
	private ArrayList<VolumeNecessaryDetail> _volumeNecessaryDetails = new ArrayList<VolumeNecessaryDetail>();

	public VolumeNecessary(DataRow dataRow)
	{
		this._id = (Guid) dataRow.get_Item("ID");
		this._option = Enum.valueOf(VolumeNecessaryOption.class, (Short)dataRow.get_Item("Option"));
	}

	public VolumeNecessary(XmlNode node)
	{
		this.setValue(node.get_Attributes());
	}

	public Guid get_Id()
	{
		return this._id;
	}

	public VolumeNecessaryOption get_Option()
	{
		return this._option;
	}

	public void setValue(XmlAttributeCollection attributeCollection)
	{
		for (int i = 0; i < attributeCollection.get_Count(); i++)
		{
			String nodeName = attributeCollection.get_ItemOf(i).get_LocalName();
			String nodeValue = attributeCollection.get_ItemOf(i).get_Value();
			if (nodeName.equals("ID"))
			{
				this._id = new Guid(nodeValue);
			}
			else if (nodeName.equals("Option"))
			{
				this._option = Enum.valueOf(VolumeNecessaryOption.class, Integer.parseInt(nodeValue));
			}
		}
	}

	public void add(VolumeNecessaryDetail item)
	{
		int position = this.getPositionOf(item);
		if(position == -1)
		{
			this._volumeNecessaryDetails.add(item);
		}
		else
		{
			this._volumeNecessaryDetails.add(position, item);
		}
	}

	public void remove(Guid volumeNecessaryDetailId)
	{
		VolumeNecessaryDetail detail = this.get(volumeNecessaryDetailId);
		if(detail != null)
		{
			this._volumeNecessaryDetails.remove(detail);
		}
	}

	public VolumeNecessaryDetail get(Guid volumeNecessaryDetailId)
	{
		for(int index = 0; index < this._volumeNecessaryDetails.size(); index++)
		{
			if(this._volumeNecessaryDetails.get(index).get_Id().compareTo(volumeNecessaryDetailId) == 0)
			{
				return this._volumeNecessaryDetails.get(index);
			}
		}
		return null;
	}

	private int getPositionOf(VolumeNecessaryDetail item)
	{
		int position = -1;
		for(int index = 0; index < this._volumeNecessaryDetails.size(); index++)
		{
			if(this._volumeNecessaryDetails.get(index).get_From() > item.get_From())
			{
				position = index;
				break;
			}
		}
		return position;
	}

	public double calculateMargin(double marginRate, double defaultMargin, double netLot, boolean useMarginD)
	{
		if(this._volumeNecessaryDetails.size() == 0) return marginRate * defaultMargin * netLot;

		if(this._option.value() == VolumeNecessaryOption.Flat.value())
		{
			double margin = defaultMargin;
			for(int index = 0; index < this._volumeNecessaryDetails.size() - 1; index++)
			{
				VolumeNecessaryDetail volumeNecessaryDetail = this._volumeNecessaryDetails.get(index);
				VolumeNecessaryDetail nextVolumeNecessaryDetail = this._volumeNecessaryDetails.get(index + 1);
				if(netLot > volumeNecessaryDetail.get_From() && netLot <= nextVolumeNecessaryDetail.get_From())
				{
					margin = useMarginD ? volumeNecessaryDetail.get_MarginD() : volumeNecessaryDetail.get_MarginO();
					break;
				}
			}

			VolumeNecessaryDetail volumeNecessaryDetail = this._volumeNecessaryDetails.get(this._volumeNecessaryDetails.size() - 1);
			if(netLot > volumeNecessaryDetail.get_From())
			{
				margin = useMarginD ? volumeNecessaryDetail.get_MarginD() : volumeNecessaryDetail.get_MarginO();
			}

			return marginRate * margin * netLot;
		}
		else if(this._option.value() == VolumeNecessaryOption.Progessive.value())
		{
			double necessary = marginRate * defaultMargin * Math.min(netLot, this._volumeNecessaryDetails.get(0).get_From());

			int index = 0;
			while (index < this._volumeNecessaryDetails.size() && netLot > this._volumeNecessaryDetails.get(index).get_From())
			{
				VolumeNecessaryDetail volumeNecessaryDetail = this._volumeNecessaryDetails.get(index);
				double margin = useMarginD ? volumeNecessaryDetail.get_MarginD() : volumeNecessaryDetail.get_MarginO();
				double lot = netLot - volumeNecessaryDetail.get_From();
				if (index < this._volumeNecessaryDetails.size() - 1)
				{
					VolumeNecessaryDetail nextVolumeNecessaryDetail = this._volumeNecessaryDetails.get(index + 1);
					lot = Math.min(lot, (nextVolumeNecessaryDetail.get_From() - volumeNecessaryDetail.get_From()));
				}
				necessary += marginRate * margin * lot;

				index++;
			}

			return necessary;
		}
		else
		{
			return 0.0;
		}
	}

	public static void update(SettingsManager settingsManager, XmlNode xmlNode, String updateType)
	{
		if (xmlNode == null) return;

		if (updateType.equals("Modify") || updateType.equals("Add"))
		{
			XmlAttributeCollection attributeCollection = xmlNode.get_Attributes();
			Guid id = new Guid(attributeCollection.get_ItemOf("ID").get_Value());
			VolumeNecessary volumeNecessary = settingsManager.getVolumeNecessary(id);
			if(volumeNecessary == null)
			{
				volumeNecessary = new VolumeNecessary(xmlNode);
				settingsManager.addVolumeNecessary(volumeNecessary);
			}
			else
			{
				volumeNecessary.setValue(attributeCollection);
			}
		}
	}
}
