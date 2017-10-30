The file "ProfidataPreferences.epf" contains all the preferences to import into
your workspace. It has been manually created by concatenating all the .epf files
from the ./source folder. Most of these source files have been created manually
by copying the properties from a complete preference export.

If any of the source files has been changed, recreated the file "ProfidataPreferences.epf" 
by concatenating all the source files in the order they are listed below, commit
all the changed files to the source code repository and notify your collegues.

- Java Compiler.epf: Export -> Preferences: Java Compiler Preferences
- Aspectj Ajdt.epf: manually created
  + from a complete preference export copy all the lines starting with 
    '/instance/org.eclipse.ajdt.ui' except '/instance/org.eclipse.ajdt.ui/ajde.version.at.previous.startup'
- PDE Compiler.epf: manually created
  + from a complete preference export copy all the lines starting with
    '/instance/org.eclipse.pde/compilers'
    
- Java Codestyle.epf: Export -> Preferences: Java Code Style Preferences
  + remove the lines starting with 
    o '/instance/org.eclipse.jdt.ui/org.eclipse.jdt.ui.cleanupprofiles'
    o '/instance/org.eclipse.jdt.ui/org.eclipse.jdt.ui.formatterprofiles'
    o '/instance/org.eclipse.jdt.ui/org.eclipse.jdt.ui.text.custom_code_templates'
    because they are available as .xml files in the formatter section of this project.
- Java Save.epf: manually created
  + from a complete preference export copy all the lines starting with
    '/instance/org.eclipse.jdt.ui/sp_cleanup' and add the line 
    '/instance/org.eclipse.jdt.ui/editor_save_participant_org.eclipse.jdt.ui.postsavelistener.cleanup=true'

- Team Ignored.epf: manually created
  + from a complete preference export copy all the lines starting with
    '/instance/org.eclipse.team.core/ignore_files'

- IDE PerspectiveSwitcher.epf: manually created
  + from a complete preference export copy all the lines starting with
    '/instance/org.eclipse.ui/SHOW_TEXT_ON_PERSPECTIVE_BAR'
    
------------------------------------------------------------------------------------------------------

The file "Profidata_GrepConsole.xml" was exported from the GrepConsole plugin
which can be installed from: http://eclipse.schedenig.name/

The XML file contains a useful configuration for XC Client and Server.

To import the XML file:
- go to Eclipse Preferences
- select 'Grep Console'
- if you have old 'Expressions' and 'Styles' I would recommend to delete them
  (or rename the folder to something else than 'Imported', so that you can distinguish old from new)
- In 'Grep Console/Settings' set the
  Style Match Length: 300 

