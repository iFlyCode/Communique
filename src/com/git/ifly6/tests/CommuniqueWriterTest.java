package com.git.ifly6.tests;

import com.git.ifly6.communique.data.CommuniqueRecipient;
import com.git.ifly6.communique.data.CommuniqueRecipients;
import com.git.ifly6.communique.io.CommuniqueConfig;
import com.git.ifly6.communique.io.CommuniqueLoader;
import com.git.ifly6.communique.io.CommuniqueProcessingAction;
import com.git.ifly6.javatelegram.JTelegramKeys;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class CommuniqueWriterTest {

	public static void main(String[] args) throws IOException {
		CommuniqueConfig config = new CommuniqueConfig(true, CommuniqueProcessingAction.NONE,
				new JTelegramKeys("client-key", "secret-key", "telegram-id"));

		CommuniqueRecipient[] recipients = new CommuniqueRecipient[]{
				CommuniqueRecipients.createFlag("recruit"),
				CommuniqueRecipients.createExcludedNation("excluded1"),
				CommuniqueRecipients.createExcludedNation("excluded2"),
				CommuniqueRecipients.createExcludedNation("excluded3"),
				CommuniqueRecipients.createExcludedNation("excluded4"),
				CommuniqueRecipients.createExcludedNation("excluded5"),
				CommuniqueRecipients.createExcludedNation("excluded6"),
				CommuniqueRecipients.createExcludedNation("excluded7"),
				CommuniqueRecipients.createExcludedNation("excluded8"),
				CommuniqueRecipients.createExcludedNation("excluded9")
		};

		config.setcRecipients(Arrays.asList(recipients));
		Path path = Paths.get("test-output.json");
		CommuniqueLoader loader = new CommuniqueLoader(path);
		loader.save(config);

		assert Files.exists(path); // make sure file is in fact output
		assert config.getcRecipientsString().size() == recipients.length; // make sure lengths preserved
		assert config.getcRecipients().equals(Arrays.asList(recipients)); // make sure saving is correct

	}

}
