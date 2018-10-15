package com.git.ifly6.tests;

import com.git.ifly6.communique.data.Communique7Parser;
import com.git.ifly6.communique.data.CommuniqueRecipient;
import com.git.ifly6.communique.data.CommuniqueRecipients;
import com.git.ifly6.communique.data.FilterType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Communique7ParserTranslateTest {

	// testing methods for the parser
	public static void main(String[] args) {

		Scanner scan = new Scanner(System.in);
		System.out.print("[translate], [parser]?\t");
		String input = scan.nextLine();

		assert translate("wa:members") == CommuniqueRecipients.createTag(FilterType.NORMAL, "wa");
		assert translate("region:Europe") == CommuniqueRecipients.createRegion(FilterType.NORMAL, "Europe");
		assert translate("/region:Europe") == CommuniqueRecipients.createRegion(FilterType.EXCLUDE, "Europe");
		assert translate("/wa:delegates") == CommuniqueRecipients.createTag(FilterType.EXCLUDE, "delegates");

		if ("translate".equals(input)) {

			List<String> list = Arrays.asList("wa:members", "tag:wa", "region:Europe -> wa:members", "wa:delegates",
					"tag:new", "world:new", "imperium anglorum", "/imperium anglorum", "region:Europe -- wa:members");
			System.out.println(CommuniqueRecipient.translateTokens(list).stream()
					.collect(Collectors.joining("\n")));

			System.out.println();

			System.out.println(CommuniqueRecipient
					.translateTokens(Collections.singletonList("region:Europe -> wa:members")));

		} else if ("parser".equals(input)) {

			Communique7Parser ps = new Communique7Parser();

			List<CommuniqueRecipient> blah = new ArrayList<>();
			blah.add(CommuniqueRecipients.createNation("hi ya"));
			blah.add(CommuniqueRecipients.createNation("hi ya"));
			blah.add(CommuniqueRecipients.createNation("hi ya"));
			blah.add(CommuniqueRecipients.createNation("hi ya"));

			ps.apply(blah);
			System.out.println(ps.listRecipients());

		}

		scan.close();

	}
	/**
	 * This method is syntactical sugar for the translation process in {@link CommuniqueRecipient#translateTokens(List)}.
	 * That method is applied on lists because some of the old tokens, e.g. <code>region:europe -> wa:members</code>
	 * now translates to <code>region:europe, +tag:wa</code>, which is technically two statements.
	 *
	 * @param s is a <code>String</code> in the old style to translate
	 * @return the {@link CommuniqueRecipient} that is translated
	 */
	private static CommuniqueRecipient translate(String s) {
		List<String> list = CommuniqueRecipient.translateTokens(Collections.singletonList(s));
		if (list.size() > 0)
			return CommuniqueRecipient.parseRecipient(list.get(0));

		throw new IllegalArgumentException("Problem with parsing " + s);
	}

}
