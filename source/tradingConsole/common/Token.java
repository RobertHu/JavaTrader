package tradingConsole.common;

import framework.Guid;

public class Token
{
	private Guid _userId;
	private UserType _userType;
	private AppType _appType;
	private String _sessionId;

	public Guid get_UserId()
	{
		return this._userId;
	}

	public UserType get_UserType()
	{
		return this._userType;
	}

	public AppType get_AppType()
	{
		return this._appType;
	}

	public String get_SessionId()
	{
		return this._sessionId;
	}

	public static boolean equals2(Token token, Token token2)
	{
		return ( (Object) token == null && (Object) token2 == null) || token.equals(token2);
	}

	public static boolean unequals(Token token, Token token2)
	{
		return! (token == token2);
	}

	//public override bool equals(Object obj)
	public boolean equals(Object obj)
	{
		if (obj == null || this.getClass() != obj.getClass())
		{
			return false;
		}
		Token token = (Token) obj;

		return (this._userId.equals(token._userId) &&
				this._userType.equals(token._userType) &&
				this._appType.equals(token._appType));
	}

	//public override int getHashCode()
	public int getHashCode()
	{
		int hashCode = this._userId.getHashCode();
		hashCode ^= this._userType.hashCode();
		hashCode ^= this._appType.hashCode();

		return hashCode;
	}

	//public override String toString()
	public String toString()
	{
		java.lang.StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append(this._userId);
		stringBuffer.append(this._userType);
		stringBuffer.append(this._appType);
		stringBuffer.append(this._sessionId);
		return stringBuffer.toString();
		//return String.format("Token {0},{1},{2},{3}", this.userID, this.userType, this.appType, this.sessionID);
	}

	public int compareTo(Object obj)
	{
		int result = 0;
		Token token = (Token) obj;

		result = this._userId.compareTo(token._userId);
		if (result != 0)
		{
			return result;
		}
		result = this._userType.compareTo(token._userType);
		if (result != 0)
		{
			return result;
		}
		result = this._appType.compareTo(token._appType);

		return result;
	}

	public Token(Guid userID, UserType userType, AppType appType)
	{
		this._userId = userID;
		this._userType = userType;
		this._appType = appType;
	}
}
