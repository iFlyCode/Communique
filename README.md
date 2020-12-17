# Communiqué #

## Summary ##
Communiqué is a free, open-source, and platform-independent client for NationStates's Telegram API. It is directly based on top of 
[JavaTelegram](https://github.com/iFlyCode/NationStates-JavaTelegram) a library of mine designed to interface 
with NationStates itself.

If you want to run headless on a Linux box or just broadly from the command line, you should download the Marconi executable.

It is *your* responsibility to know how the telegram API works. Use of this program is agreement that you understand 
those limitations and requirements. It also agrees that you cannot claim losses, damages, or other negative effects 
from the author if action is taken against your NationStates account or from other activity.

## Documentation ##

### System Requirements ###
* Java JRE 8 (https://java.com/en/download/)
* A [NationStates](https://www.nationstates.net) nation
* A NationStates [API client key](https://www.nationstates.net/pages/api.html#telegrams)
* A NationStates [telegram ID and secret key](https://www.nationstates.net/pages/api.html#telegrams)

### Readme ###
1. Acquire an API client key. You will need to contact the NationStates moderators with a GHR (Getting Help Request) 
for one. You will need to provide the region for whom the client key is made (there can only be one per region), the 
person to whom the key is responsible, the manner by which you will use it, and the purpose for which you requested it.

2. Acquire your secret key and telegram id. They are unique keys for each telegram. Type these keys into the boxes at 
the bottom.

3. Make sure that your telegram follows the rate limit by using the checkbox.

4. Go to the recipients tab and add in your nations. There are a number of tags supported in the program as well as a 
NOT tag.
   - Nations must be declared in the following syntax `nation:[name]`. They can be separated by commas or new lines.
   - Regions must be declared with the following syntax `region:[name]` and can also be separated by commas or new 
   lines.
   - There are three tags supported in Communiqué, `tag:wa`, `tag:delegates`, and `tag:new`. The first is the list of 
   all World Assembly members. The second is the list of all World Assembly delegates. The third is a list of new 
   nations.
   - There are two filters which can be applied by prefixing `+` or `-`. `+` is an intersection operator, which 
   requires that all preceding recipients also meet the criteria put with `+`. `-` is an exclusion operator, which 
   requires that all preceding recipients be removed if they match criteria.
     - For example, `region:Europe, +tag:WA` would find all members of Europe, and then return the list of nations 
     which are also WA members.
     - `region:Europe, -tag:WA` does the opposite, where it finds all member of Europe and then removes all WA members.
     - While we strongly recommend putting each recipient on each line, it is possible to use commas as well.
   - This format system should be identical with the vanilla recipients system used in NationStates 
   - In build `6`, a Recruitment wizard was added. Simply use `flag:recruit` (or go into the menu) as your recipient
   to bring it up. Note that `flag:recruit` is not compatible with sending a telegram to any other nations. You can also
   specify regions to exclude from recruitment using the `-region:[name]` syntax.

5. Click the 'Parse' button to see a list of all your recipients. When ready, click the 'Send' button to send the 
telegrams.

6. You can save all the keys and all your recipients to a file. The file (if your calls completed without any errors) 
will include your client, secret, and telegram keys; as well as the recipients of the file, and the people to which the 
file was already sent. Do not share this file. You can load these files from disc to restore a previous configuration 
(or, create them by hand and skip manual configuration).

### Technical Information ####
This program reports its UserAgent to the NationStates API as follows:
* `NationStates JavaTelegram (maintained by Imperium Anglorum, used by <client key>)` when sending telegrams and
* `NS API request; maintained by Imperium Anglorum, email: cyrilparsons.london@gmail.com; see IP.` when requesting information from the API
	

## Changelog ##
The following are the various changelogs from various builds of Communique:

1. **Version 1.0.** Build 1 comes in three flavours, Communiqué (GUI), Morse (old CLI), and Marconi (new CLI).
   - Communiqué has support for the keys, variable flags, and the saving and loading of the program configuration. 
   - Morse does not do any of this. It only has support for a simple list of recipients without support for loading or 
   saving of anything more than the client key. 
   - Marconi is a headless client. It will only accept valid files from Communiqué and then read those files and use 
   all the data in them. It does not write these files (other than updating their sent lists). 
   - Note that Morse will likely be deprecated in favour of just Communiqué and Marconi. *Morse deleted.*

2. Build 2 is a change due to the introduction of a new operator which is written to file. The `->` operator, 
signifying 'in', cannot be parsed by build 1 parsers, and hence, necessitated a change in the build number.
   - Added `->` modifier, signifying 'in', e.g. `region:europe -> wa:members` would return 'nations of Europe in 
   WA:members' and `region:europe -> wa:delegates` would return the Delegate of Europe. Due to the way the 'both' 
   function works, it is commutative.

3. Build 3 is a cosmetic upgrade. Communiqué switched from Java's Swing to JavaFX. Build 3 uses the same file 
format for build 2. It also has a massive number of bug fixes and optimisations which were developed in the time 
between builds 2 and 3.

4. Build 4 introduces a flag into the file system to randomise the list of recipients. This flag is defined by 
the `randomSort` flag inside the file system and a system for generating that flag has been added to Communiqué. 

5. Build 5 introduces a new operator which is written to file along with a new flag. 
   - The `--` operator, signifying 'not', cannot be parsed by previous builds, necessitating a build number change. It 
   allows for local negation instead of a global negation as `/` does. The `--` allows for one to specify 
   `region:europe -- wa:members` followed by `region:europe -> wa:delegates`. Since delegates are a subset of members, 
   this would be impossible with global negation.
   - Version 5 also transitions to a new UI and back to Swing instead of JavaFX. It also works to eliminate the use of 
   multiple tabs and put all relevant information at your fingertips.
   - Note that build 5 was never released to the public and exists as a development bridge to build 6.

6. Build 6 introduces a new window to the program, called the Communiqué Recruiter for recruiting nations. It also 
has a built-in filter to check nations and make sure that you can exclude certain feeders. This is accessible using 
commands `flag:recruit` and `flag:recruit -- region:x` or using the set-up wizard.
   - There have also been significant changes to the program's interface, being entirely rebuilt in Swing, with 
   accelerators and other improvements — like on the fly alerts that inform the user immediately instead of waiting.
   - A logging system has also been built to export relevant logs to a text area and print them to file.
   - Finished a small web-scraper that parses out the delegates supporting and opposing some motion in the World 
   Assembly.

7. **Version 2.0.** Build 7 rehashes the entire Communiqué syntax structure to operate in line with the NationStates telegram API 
system. ~~Thus, a query in the NationStates API like `region:Europe, +tag:WA` will function exactly the same in 
Communiqué.~~
   - This build also switches to HTTPS per the recent NationStates update and implements a new update-checker (it 
   will check, at most, once a week). 
   - It also implements a change in how telegram requests are queued. Before, a time delay would commence, and after 
   that delay, the program would check for whether the recipient is a valid recipient. Now, during that time delay, the 
   program checks for whether the recipient is valid, saving around 1.6 seconds from every recruitment call. When not on
   recruitment, it also checks whether that nation has opt-ed into campaign telegrams, and if not, skips to the next 
   recipient.
   - Version 7.1 overhauls the UI present since build 5. This eliminates the second text area showing the parsed 
   recipients and creates a dialog which shows them. It also puts out a bug fix to the web scraper introduced in 
   build 6 and increases the number of tags which the automatic token translation system can handle. 

8. Build 8 introduces a new GUI and overhauls the post-processing system. Before, the use of a boolean flag for `randomSort` meant that you could only sort things randomly. Now, Communiqué supports randomisation of the recipients list, prioritisation of the Delegates in the recipients, and reversing the order of the recipients. Due to changes in the way this is implemented, this requires a new file version; it also means that it can easily be extended. Please make any requests for new post-processing options via GitHub.

9. Build 9 adds the ability to scan nations mentioned in the NS API Happenings for activity and return their names for possible telegram dispatch. Implementation of this new command requires a new build number as it is not backwards compatible with older builds.

10. Build 10 allows names to be filtered using `+regex:PATTERN` and `-regex:PATTERN` flags that require and omit regex matches, respectively. Obviously, these flags break compatibility with previous builds. If there are further kinds of filters desired, make a feature request on GitHub.
    - As this operates on the Java `Pattern` library, anything that fails in that library—which I don't expect will be often—will also fail here. There may be issues when using `:` in any regex because of the way that Communique parses names. But `:` doesn't mean anything special, so that should not be much of a problem.
    - Hint: `-regex:.*[0-9]$` will omit anything that ends with a digit
    - Hint: `+regex:[a-zA-Z]+` will require only characters
    
11. Build 11 introduces regional tags searches. These will usually take a long time because there are lots of regions and each regions must be polled individually. There is also now greater coverage of the internal parser in the logs: if you use the regional tags and want updates on parsing, open in a command line.
    - Hint: `region_tag:*,-tiny,-small,-minuscule` will probably remove most of the small regions from your query.
    - Important note: The program no longer automatically separates items within a single line with commas. This is to facilitate the use of the NS API tag queries. Thus, `region:Europe, +tag:wa` will now throw an error unless on two different lines. Previous configuration files already split those lines automatically, so old configuration files should work without changes.
    - Back-end note: NS JavaTelegram has now been subsumed into Communique.
    
12. Build 12 allows typing of telegrams to specify default delay times and also introduces overriding the default times with wait time input. Both changes affect configuration file format.

## Road ahead
In a future build, I intend to phase out the concept of a separate recruiter and simply permit someoneone to specify that some action be taken repeatedly. Some syntax like `flag:repeat; limit:1; tag:new`.

Another possibility is adding exit or entrance listeners. Perhaps something like `flag:exit,europe`. Ideas are as of yet vague.