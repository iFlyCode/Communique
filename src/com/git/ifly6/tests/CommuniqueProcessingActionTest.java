package com.git.ifly6.tests;

import com.git.ifly6.communique.data.CommuniqueRecipient;
import com.git.ifly6.communique.io.CommuniqueProcessingAction;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.git.ifly6.communique.data.CommuniqueRecipients.createNation;

public class CommuniqueProcessingActionTest {


	public static void main(String[] args) {
		List<CommuniqueRecipient> original = new ArrayList<>();
		original.add(createNation("alpha"));
		original.add(createNation("bravo"));
		original.add(createNation("charlie"));
		original.add(createNation("delta"));
		original.add(createNation("echo"));
		original.add(createNation("foxtrot"));
		original.add(createNation("golf"));

		List<String> input = original.stream().map(CommuniqueRecipient::toString).collect(Collectors.toList());
		System.out.println(CommuniqueProcessingAction.REVERSE.apply(input));
		System.out.println(CommuniqueProcessingAction.RANDOMISE.apply(input));
		System.out.println(CommuniqueProcessingAction.NONE.apply(input));
	}
}
