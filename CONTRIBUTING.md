# Liquid Crystal Studios Contribution Guide

## Code Style
[Style Guide](STYLEGUIDE.md)

## Branches
When developing, one must create a branch and then submit a merge request when finished with the branch.
There are two types of branches that are acceptable.

### Working Branches
A branch labeled `working-[username]` can be used when making changed that are not related to an issue. An example of such branch is: `working-trevorflynn`, this would indicate that Trevor Flynn has created this branch.
Not all working branches will get accepted when a merge request is accepted.  
A branch will not be accepted when:
* Conflicts with master branch
* Does not pass pipeline build
* Does not provide any contribution
* Contains improperly formatted code
* Contains a feature or fix that was not approved.

### Issue Branches
When working on an issue or feature that is documented in issues, a branch must be created and its name must start with the ID of the issue.  
An example would be if someone was fixing issue #45, an error. Then the branch name must start with 45. i.e. `45-fix-for-some-error`  
A merge request must be made before the branch will be merged with the master branch.  
A branch will not be accepted when:
* Conflicts with master branch
* Does not pass pipeline build
* Does not provide any contribution
* Contains improperly formatted code
* Contains a feature or fix that was not approved.

### The Master Branch
The master branch is the current development branch that all work will be merged into.

### Production Branches
A production branch will deploy the application.  
The branch must be named: `[release issue #]-release-v[version]`  
Example: `33-release-v0.1.1`  

## Issues

### Tagging
If you find a bug, security exploit, or feature. Please create an issue with the appropriate tags.  
Do not attach the `issue` tag to something that is not a bug or security exploit.
Do not attach the `feature` tag to something that is not a possible feature or enhancement.
Do not attach a group tage such as `front-end` or `back-end` to an issue that does not pertain to the group.
Do not attach a `review` or `second-opinion` tag to issues that do not need feedback from Trevor Flynn or Jayce Miller respectively.
Attach a code tag if the issue pertains to a type of code. For example, if there is an issue inside of the database, attach the `database` tag.  

### Assigning
Do not assign an issue to someone other than yourself.  
Do not assign on issue to yourself if you are not going to work on it.  
(The above rules do not seem to apply to Trevor Flynn...)

### Closing
Do not close an issue unless it is agreed that it should be closed, or until the issue is resolved.

