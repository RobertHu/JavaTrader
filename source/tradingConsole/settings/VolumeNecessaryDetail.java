package tradingConsole.settings;

import java.math.*;

import framework.*;
import framework.data.*;
import framework.xml.*;
import tradingConsole.*;

public class VolumeNecessaryDetail
{
	private Guid _id;
	private Guid _volumeNecessaryId;
	private BigDecimal _from;
	private BigDecimal _marginD;
	private BigDecimal _marginO;

	public VolumeNecessaryDetail(DataRow dataRow)
	{
		this._id = (Guid) dataRow.get_Item("Id");
		this._volumeNecessaryId = (Guid) dataRow.get_Item("VolumeNecessaryId");
		this._from = AppToolkit.convertDBValueToBigDecimal(dataRow.get_Item("From"), 0.0);
		this._marginD = AppToolkit.convertDBValueToBigDecimal(dataRow.get_Item("MarginD"), 0.0);
		this._marginO = AppToolkit.convertDBValueToBigDecimal(dataRow.get_Item("MarginO"), 0.0);
	}

	public VolumeNecessaryDetail(XmlNode node)
	{
		this.setValue(node.get_Attributes());
	}

	public Guid get_Id()
	{
		return this._id;
	}

	public Guid get_VolumeNecessaryId()
	{
		return this._volumeNecessaryId;
	}

	public double get_From()
	{
		return this._from.doubleValue();
	}

	public double get_MarginD()
	{
		return this._marginD.doubleValue();
	}

	public double get_MarginO()
	{
		return this._marginO.doubleValue();
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
			else if (nodeName.equals("VolumeNecessaryId"))
			{
				this._volumeNecessaryId = new Guid(nodeValue);
			}
			else if (nodeName.equals("From"))
			{
				this._from = new BigDecimal(nodeValue);
			}
			else if (nodeName.equals("MarginD"))
			{
				this._marginD = new BigDecimal(nodeValue);
			}
			else if (nodeName.equals("MarginO"))
			{
				this._marginO = new BigDecimal(nodeValue);
			}
		}
	}

	public static void update(SettingsManager settingsManager, XmlNode xmlNode, String updateType)
	{
		if (xmlNode == null) return;

		XmlAttributeCollection attributeCollection = xmlNode.get_Attributes();
		Guid id = new Guid(attributeCollection.get_ItemOf("ID").get_Value());
		Guid volumeNecessaryId = new Guid(attributeCollection.get_ItemOf("VolumeNecessaryId").get_Value());
		VolumeNecessary volumeNecessary = settingsManager.getVolumeNecessary(volumeNecessaryId);
		if(volumeNecessary != null)
		{
			if (updateType.equals("Delete"))
			{
				volumeNecessary.remove(id);
				return;
			}

			VolumeNecessaryDetail volumeNecessaryDetail = volumeNecessary.get(id);
			if(volumeNecessaryDetail == null)
			{
				volumeNecessaryDetail = new VolumeNecessaryDetail(xmlNode);
				volumeNecessary.add(volumeNecessaryDetail);
			}
			else
			{
				volumeNecessaryDetail.setValue(attributeCollection);

				//readd the detail, for the Form of it may changed
				volumeNecessary.remove(volumeNecessaryDetail.get_Id());
				volumeNecessary.add(volumeNecessaryDetail);
			}
		}
	}
}
