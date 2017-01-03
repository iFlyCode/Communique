# Communiqué #

## Summary ##
Communiqué is a free, open-source, and platform-independent client for NationStates's Telegram API. It was originally inspired by Auralia's work on the matter and thus, recognised the limitations of writing on a Windows-only platform when most headless servers are Linux boxes. It is directly based on top of [JavaTelegram](https://github.com/iFlyCode/NationStates-JavaTelegram) a iFlyCode Java library designed to interface with NationStates itself.

It is *your* responsibility to know how the telegram API works. Use of this program is agreement that you understand those limitations and requirements. It also agrees that you cannot claim losses, damages, or other negative effects from the author if action is taken against your NationStates account.

## Documentation ##

### System Requirements ###
* Java JRE 8 (https://java.com/en/download/)
* A [NationStates](http://www.nationstates.net) nation
* A NationStates [API client key](http://www.nationstates.net/pages/api.html#telegrams)
* A NationStates [telegram ID and secret key](http://www.nationstates.net/pages/api.html#telegrams)

### Readme ###
1. Acquire an API client key. You will need to contact the NationStates moderators with a GHR (Getting Help Request) for one. You will need to provide the region for whom the client key is made (there can only be one per region), the person to whom the key is responsible, the manner by which you will use it, and the purpose for which you requested it.

2. Acquire your secret key and telegram id. They are unique keys for each telegram. Type these keys into the boxes at the bottom.

3. Make sure that your telegram follows the rate limit by using the checkbox.

4. Go to the recipients tab and add in your nations. There are a number of tags supported in the program as well as a NOT tag.
  - Nations must be declared in the following syntax `nation:[name]`. They can be separated by commas or new lines.
  - Regions must be declared with the following syntax `region:[name]` and can also be separated by commas or new lines.
  - There are three tags supported in Communiqué, `tag:wa`, `tag:delegates`, and `tag:new`. The first is the list of all World Assembly members. The second is the list of all World Assembly delegates. The third is a list of new nations.
  - There are two filters which can be applied by prefixing `+` or `-`. `+` is an intersection operator, which requires that all preceding recipients also meet the criteria put with `+`. `-` is an exclusion operator, which requires that all preceding recipients be removed if they match criteria.
  	- For example, `region:Europe, +tag:WA` would find all members of Europe, and then return the list of nations which are also WA members.
  	- `region:Europe, -tag:WA` does the opposite, where it finds all member of Europe and then removes all WA members.
  	- While we strongly recommend putting each recipient on each line, it is possible to use commas as well.
  - This format system should be identical with the vanilla recipients system used in NationStates 
  - In version `6`, a Recruitment wizard was added. Simply use `flag:recruit` (or go into the menu) as your recipient to bring it up. Note that `flag:recruit` is not compatible with sending a telegram to any other nations. You can also specify regions to exclude from recruitment using the `-region:[name]` syntax.

5. Click the 'Parse' button to see a list of all your recipients. When ready, click the 'Send' button to send the telegrams.

6. You can save all the keys and all your recipients to a file. The file (if your calls completed without any errors) will include your client, secret, and telegram keys; as well as the recipients of the file, and the people to which the file was already sent. Do not share this file. You can load these files from disc to restore a previous configuration (or, create them by hand and skip manual configuration).

### Technical Information ####
* This program reports its UserAgent to the NationStates API as follows:
	`NationStates JavaTelegram (maintained by Imperium Anglorum, used by <client key>)`

## Changelog ##
While there will be a version number, currently, there is no such number, since, for obvious reasons, we are still in alpha. Version numbers will start after the program is released.

1. Version 1 comes in three flavours, Communiqué (GUI), Morse (old CLI), and Marconi (new CLI).
  - Communiqué has support for the keys, variable flags, and the saving and loading of the program configuration. 
  - Morse does not do any of this. It only has support for a simple list of recipients without support for loading or saving of anything more than the client key. 
  - Marconi is a headless client. It will only accept valid files from Communiqué and then read those files and use all the data in them. It does not write these files (other than updating their sent lists). 
  - Note that Morse will likely be phased out in favour of just Communiqué and Marconi.

2. Version 2 is a change due to the introduction of a new operator which is written to file. The `->` operator, signifying 'in', cannot be parsed by version 1 parsers, and hence, necessitated a change in the version number.
  - Added `->` modifier, signifying 'in', e.g. `region:europe -> wa:members` would return 'nations of Europe in WA:members' and `region:europe -> wa:delegates` would return the Delegate of Europe. Due to the way the 'both' function works, it is commutative.

3. Version 3 is a cosmetic upgrade. Communiqué switched from Java's Swing to JavaFX. Version 3 uses the same file format for Version 2. It also has a massive number of bug fixes and optimisations which were developed in the time between Versions 2 and 3.

4. Version 4 introduces a flag into the file system to randomise the list of recipients. This flag is defined by the `randomSort` flag inside the file system and a system for generating that flag has been added to Communiqué. 

5. Version 5 introduces a new operator which is written to file along with a new flag. 
  - The `--` operator, signifying 'not', cannot be parsed by previous versions, necessitating a version change. It allows for local negation instead of a global negation as `/` does. The `--` allows for one to specify `region:europe -- wa:members` followed by `region:europe -> wa:delegates`. Since delegates are a subset of members, this would be impossible with global negation.
  - Version 5 also transitions to a new UI and back to Swing instead of JavaFX. It also works to eliminate the use of multiple tabs and put all relevant information at your fingertips.
  - Note that version 5 was never released to the public and exists as a development bridge to version 6.

6. Version 6 introduces a new window to the program, called the Communiqué Recruiter for recruiting nations. It also has a built-in filter to check nations and make sure that you can exclude certain feeders. This is accessible using commands `flag:recruit` and `flag:recruit -- region:x` or using the set-up wizard.
  - There have also been significant changes to the program's interface, being entirely rebuilt in Swing, with accelerators and other improvements — like on the fly alerts that inform the user immediately instead of waiting.
  - A logging system has also been built to export relevant logs to a text area and print them to file.
  - Finished a small web-scraper that parses out the delegates supporting and opposing some motion in the World Assembly.

7. Version 7 rehashes the entire Communiqué syntax structure to operate in line with the NationStates telegram API system. Thus, a query in the NationStates API like `region:Europe, tag:WA` will function exactly the same in Communiqué. This version also switches to HTTPS per the recent NationStates update and implements a new update-checker (it will check, at most, once a week).