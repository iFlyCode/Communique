/* Copyright (c) 2016 Kevin Wong. All Rights Reserved. */
package com.git.ifly6.communique.data;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

/** <code>Communique7Parser</code> is the new parser designed for Communique 7, which implements the same way to declare
 * recipients as used in NationStates. It supersedes the old parser, {@link CommuniqueParser}, which used the custom
 * recipient declaration system in older versions of Communique.
 * <p>
 * <code>Communique7Parser</code> also provides methods to translate between the old and new Communique address tokens,
 * allowing for a seam-less transition between the old and new token systems.
 * </p>
 * @author ifly6 */
public class Communique7Parser {
	
	/** Declares the version of the parser, which is based on two values: (1) the syntax of the Communique recipients
	 * language and (2) the file syntax in which that information is held. */
	public static final int version = 7;
	
	/** List of recipients changed by various actions and applications called by the parser. */
	private Set<CommuniqueRecipient> recipients = new LinkedHashSet<>(0);
	
	/** Creates a new empty parser without any applied tokens. To actually use the parser, apply tokens using the apply
	 * methods, either in the form of a <code>List&lt;String&gt;</code> or any number of
	 * <code>CommuniqueRecipient</code>. */
	public Communique7Parser() {
	}
	
	/** Applies the tokens, specified in the <code>CommuniqueRecipient</code> object, to the recipients list in the
	 * parser.
	 * @param token a <code>CommuniqueRecipient</code>
	 * @return this parser for further token applications, if necessary */
	public Communique7Parser apply(CommuniqueRecipient token) {
		recipients = token.getFilterType().apply(recipients, token);
		/* This is the beautiful part, because I've chained everything to a filter, this means that I don't have to
		 * write any code whatsoever to sort things into what they have to do, unlike the old parser. Now, everything is
		 * chained to an ENUM which already knows exactly what it has to do, and therefore, everything is already dealt
		 * with. */
		return this;
	}
	
	/** Applies the tokens to the recipients list with a specified list of tokens. All of these should be parse-able
	 * <code>CommuniqueRecipient</code>s.
	 * @param list of parse-able <code>CommuniqueRecipient</code>s
	 * @return this object for further applications, if necessary */
	public Communique7Parser apply(List<String> list) {
		list.stream()
				.filter(s -> !s.startsWith("#"))
				.filter(s -> !StringUtils.isEmpty(s))
				.map(CommuniqueRecipient::parseRecipient)
				.forEach(this::apply);
		return this;
	}
	
	/** Applies tokens based on a variable number of <code>CommuniqueRecipient</code>s.
	 * @param tokens to apply
	 * @return this parser for further analysis if necessary */
	public Communique7Parser apply(CommuniqueRecipient... tokens) {
		Stream.of(tokens).forEach(this::apply);
		return this;
	}
	
	/** Returns all of the recipients in a <code>List</code> of <code>CommuniqueRecipient</code>.
	 * @return a list of {@link CommuniqueRecipient} */
	public List<CommuniqueRecipient> list() {
		return new ArrayList<>(recipients);
	}
	
	/** Returns all of the recipients in the standard NationStates reference name form in a <code>List</code>.
	 * @return a list of all recipients in standard reference name form */
	public List<String> getRecipients() {
		return recipients.stream()
				.map(CommuniqueRecipient::getName)
				.collect(Collectors.toList());
	}
	
	/** Translates a number of old tokens into the new Communique 7 tokens.
	 * @param oldTokens to translate
	 * @return a list of tokens which means the same thing in the new system */
	public static List<String> translateTokens(List<String> oldTokens) {
		List<String> tokens = new ArrayList<>();
		for (String oldToken : oldTokens) {
			if (oldToken.startsWith("flag:recruit")) {
				tokens.add(oldToken);
				continue;	// to next!
			}
			if (oldToken.contains("->")) {
				String[] split = oldToken.split("->");
				tokens.add(translateToken(split[0]));
				tokens.add(translateToken("->" + split[1]));
				continue;	// to next!
			}
			if (oldToken.contains("-- ")) {
				String[] split = oldToken.split("-- ");
				if (split.length == 2) {
					tokens.add(translateToken(split[0].trim()));
					tokens.add(translateToken("-- " + split[1].trim()));
					continue;	// to next!
				}
			}
			tokens.add(translateToken(oldToken));
			
		}
		return tokens;
	}
	
	/** Translates a single token from the old system to the new Communique 7 system. This method should not change any
	 * Communique 7 tokens and only translate applicable Communique 6 tokens.
	 * @param oldToken in a <code>String</code> form, like "wa:delegates"
	 * @return the token in the Communique 7 form, which, for "wa:delegates", would turn into "tag:delegates" */
	public static String translateToken(String oldToken) {
		// logic tags, somewhat recursive to ease translation of sub-tokens
		// no need to use HashMap, that seems over-engineered for something this simple
		if (oldToken.startsWith("/")) { return "-" + translateToken(oldToken.replaceFirst("/", "").trim()); }
		if (oldToken.startsWith("-- ")) { return "-" + translateToken(oldToken.replaceFirst("-- ", "").trim()); }
		if (oldToken.startsWith("-> ")) { return "+" + translateToken(oldToken.replaceFirst("->", "").trim()); }
		
		// translate tags which can be decomposed
		if (oldToken.equalsIgnoreCase("wa:delegates")) { return "tag:delegates"; }
		if (oldToken.equalsIgnoreCase("wa:members") || oldToken.equalsIgnoreCase("wa:nations")) { return "tag:wa"; }
		if (oldToken.startsWith("world:new")) { return "tag:new"; }
		
		// somewhat-direct recipient tags, like region and nation
		if (oldToken.startsWith("region:")) { return oldToken; }
		return "nation:" + oldToken;
	}
}
