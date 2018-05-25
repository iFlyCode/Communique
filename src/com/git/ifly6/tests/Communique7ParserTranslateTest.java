package com.git.ifly6.tests;

import com.git.ifly6.communique.data.Communique7Parser;
import com.git.ifly6.communique.data.CommuniqueRecipient;
import com.git.ifly6.communique.data.CommuniqueRecipients;
import com.git.ifly6.communique.data.FilterType;

import java.util.Collections;
import java.util.List;

public class Communique7ParserTranslateTest {

	public static void main(String[] args) {
		assert translate("wa:members") == CommuniqueRecipients.createTag(FilterType.NORMAL, "wa");
		assert translate("region:Europe") == CommuniqueRecipients.createRegion(FilterType.NORMAL, "Europe");
		assert translate("/region:Europe") == CommuniqueRecipients.createRegion(FilterType.EXCLUDE, "Europe");
		assert translate("/wa:delegates") == CommuniqueRecipients.createTag(FilterType.EXCLUDE, "delegates");
	}

	/**
	 * This method is syntactical sugar for the translation process in {@link Communique7Parser#translateTokens(List)}.
	 * That method is applied on lists because some of the old tokens, e.g. <code>region:europe -> wa:members</code>
	 * now translates to <code>region:europe, +tag:wa</code>, which is technically two statements.
	 *
	 * @param s is a <code>String</code> in the old style to translate
	 * @return the {@link CommuniqueRecipient} that is translated
	 */
	private static CommuniqueRecipient translate(String s) {
		List<String> list = Communique7Parser.translateTokens(Collections.singletonList(s));
		if (list.size() > 0)
			return CommuniqueRecipient.parseRecipient(list.get(0));

		throw new IllegalArgumentException("Problem with parsing " + s);
	}

}
