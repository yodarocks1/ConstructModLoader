<h1 align="center">Construct Mod Loader</h1>
<h2 align="center">Mod Loading made easy for Scrap Mechanic Survival</h2>
<p align="center">
    <a href="https://github.com/yodarocks1/ConstructModLoader/releases/latest">
        <img src="https://img.shields.io/badge/Latest Release-V+B.1.0--h1-green?style=for-the-badge&logo=github" alt="Latest Release">
    </a>
    <a href="https://discord.gg/ZcWwHeZ">
        <img src="https://img.shields.io/badge/Discord-| Join |-magenta?style=for-the-badge&logo=discord" alt="Join Discord">
    </a>
</p>
<br>
<h2>Table of Contents</h2>
<hr>
<ul>
    <li><a href="#key-features">Key Features</a></li>
    <li><a href="#installation-instructions">Installation Instructions</a></li>
    <li><a href="#running-the-program">Running the Program</a></li>
    <li><a href="#update-instructions">Update Instructions</a>
        <ul>
            <li><a href="#automatic-update">Automatic Update</a></li>
            <li><a href="#manual-update">Manual Update</a></li>
        </ul>
    </li>
    <li><a href="#explanation-of-features">Explanation of Features</a>
        <ul>
            <li><a href="#mod-merger">Mod Merger</a></li>
            <li><a href="#incompatibility-detector">Incompatibility Detector</a></li>
            <li><a href="#workshop-support">Workshop Support</a></li>
            <li><a href="#patch-system">Patch System</a></li>
            <li><a href="#steam-integration">Steam Integration</a></li>
            <li><a href="#unzipping-modsprofiles">Unzipping Mods/Profiles</a></li>
            <li><a href="#cml-console--log-system">CML Console & Log System</a></li>
            <li><a href="#profiles">Profiles</a></li>
        </ul>
    </li>
    <li><a href="#change-logs">Change Logs</a></li>
</ul>
<hr>
<h2>Key Features</h2>
<dl>
    <dt>Mod Merger  <a href="#mod-merger"><img src="https://cdn3.iconfinder.com/data/icons/eightyshades/512/40_Hyperlink-512.png" height=12 alt="_"></a></dt>
        <dd>Say goodbye to conflicts! CML uses a merging method similar to GitHub - but without the requisite user input.</dd>
    <dt>Incompatibility Detector  <a href="#incompatibility-detector"><img src="https://cdn3.iconfinder.com/data/icons/eightyshades/512/40_Hyperlink-512.png" height=12 alt="_"></a></dt>
        <dd>Don't know what's conflicting? When two mods try to modify the same line(s) of code, CML will let you know and help the user handle it!</dd>
    <dt>Workshop Support  <a href="#workshop-support"><img src="https://cdn3.iconfinder.com/data/icons/eightyshades/512/40_Hyperlink-512.png" height=12 alt="_"></a></dt>
        <dd>Download (and convert) mods from the Steam Workshop! Works with all mods demarcated as CML mods, almost all manual-install mods, and a few creative-only mods.</dd>
    <dt>Patch System  <a href="#patch-system"><img src="https://cdn3.iconfinder.com/data/icons/eightyshades/512/40_Hyperlink-512.png" height=12 alt="_"></a></dt>
        <dd>Having issues when Scrap Mechanic updates? Are mod files too large? Introducing: The Patch System! Mods that use the CML Patch System are less likely to break when Scrap Mechanic updates or when combined with other mods, and they take up very little space.</dd>
    <dt>Steam Integration  <a href="#steam-integration"><img src="https://cdn3.iconfinder.com/data/icons/eightyshades/512/40_Hyperlink-512.png" height=12 alt="_"></a></dt>
        <dd>Add Construct Mod Loader to your Steam Library with the click of a button! Automatically finds the Scrap Mechanic folder and its associated workshop location.</dd>
    <dt>Profiles  <a href="#profiles"><img src="https://cdn3.iconfinder.com/data/icons/eightyshades/512/40_Hyperlink-512.png" height=12 alt="_"></a></dt>
        <dd>Sick of keeping multiple sets of modifications? Separate them out with Profiles! Each profile can contain up to 2,147,483,647 mods, each of which can be individually enabled/disabled. You can easily switch between profiles and be happy to see that all files will be modified as expected.</dd>
    <dt></dt>
        <dd></dd>
    <dt></dt>
        <dd></dd>
</dl>
<h2>Installation Instructions</h2>
    <p>Go to the <a href="https://github.com/yodarocks1/ConstructModLoader/releases/latest">latest release</a>, then download and run the .msi file.</p>
    <p>Once you have installed CML, run it and navigate to the CML Settings tab. Press <code>(Re)-Generate Vanilla Folder</code> to use the Steam API to download/update to the latest version of Scrap Mechanic.</p>
<h2>Running the program</h2>
    <p>CML needs Administrator privileges to run if any of the following are in a special-access folder (pretty much any folder that is not a subdirectory of the Users [<code>C:\Users\<>\</code>] folder):</p>
    <ul>
        <li>Construct Mod Loader</li>
        <li>Scrap Mechanic</li>
        <li>Steam</li>
        <li>The Steam Workshop (Almost always located within the Steam folder)</li>
    </ul>
    <p>By default, the .msi will install Construct Mod Loader into <code>C:\Program Files (x86)\Construct\</code> and generate Desktop and Start Menu shortcuts for the program. It will also require Administrator privileges to run by default.</p>
    <p>If you do not have Start Menu or Desktop shortcuts for Construct Mod Loader, you can also run it from the .exe within the API folder, located at <code>C:\Program Files (x86)\Construct\API\CML.exe</code> by default.</p>
    <p>If you would like to create a Steam Shortcut for CML, you can do so by pressing the <code>Create Steam Shortcut</code> button in the CML Preferences tab. Note: Depending on your system, this may or may not run CML with Administrator privileges, but should follow the setting as defined in the Properties of the .exe on most systems.</p>
<h2>Update Instructions</h2>
<h3>Automatic Update  <img src="./src/media/MultiStateIcons/Properties/Button.png" height=32 alt="">  <sup><sub>(V+B.1.0 and up)</sub></sup></h3>
    <p>Go to the CML Preferences tab, and click <code>Check for Updates</code>. If the <code>Update</code> button enables, a new update is available - click it to automatically download and install the Update Assets from GitHub. When the download begins, the <code>Update</code> button will be disabled.</p>
    <p>In some instances, this will fail, and the <code>Update</code> button will re-enable itself automatically. In this case, press it again, and it will direct you to the GitHub page for the latest release. See the Manual Update instructions below for further instructions.</p>
<h3>Manual Update  <img src="https://simpleicons.org/icons/github.svg" height=32 alt=""></h3>
    <p>Download and unzip the UpdateAssets.zip from the <a href="https://github.com/yodarocks1/ConstructModLoader/releases/latest">latest release</a>. Place the files within the .zip into the API folder, replacing where necessary (Default location: <code>C:\Program Files (x86)\Construct\API\</code>).</p>
    <br>
    <br>
<h2>Explanation of Features</h2>
    <h3>Mod Merger</h3>
        <p></p>
    <h3>Incompatibility Detector</h3>
        <p></p>
    <h3>Workshop Support</h3>
        <p>Download and convert mods, placing them directly into the selected profile. Mods inside of profiles will retain their link to the workshop, and update as necessary.</p>
    <h3>Patch System</h3>
        <p></p>
    <h3>Steam Integration</h3>
        <p>In addition to what you see <a href="#key-features">above</a>, CML <b>will not update</b> until you tell it to. This ensures that you have time to update your mods before updating the game. You can update the game by clicking the <code>(Re)-Generate Vanilla Folder</code> button.</p>
    <h3>Unzipping Mods/Profiles</h3>
        <p>Any .zip files placed as a sibling to or a child of a profile will be automatically unzipped. This means that you can share profiles and mods via .zip files, with very little hassle.</p>
    <h3>CML Console & Log System</h3>
        <p>The CML Console will give you the ability to see everything that's going on behind the scenes. All issues will appear here. Everything will simultaneously be logged to heavy_log.xml in the API folder.</p>
    <h3>Profiles</h3>
        <p>Switch between profiles in the Profile List tab. Enable/disable mods in the Profile tab. Change the name, description, or icon of the selected profile by clicking the Profile Preferences button in the Profile tab.</p>
        <p>Note: Profiles without any mods in them will return you to a fully vanilla version.</p>
<br>
<br>
<h2>Change Logs</h2>
    <ul>
        <li><a href="./changelog/A.0.0/h1.md">A.0.0 hotfix 1</a></li>
        <li><a href="./changelog/A.0.0/h2.md">A.0.0 hotfix 2</a></li>
        <li><a href="./changelog/A.1.0/to.md">A.0.0 -> A.1.0</a></li>
        <li><a href="./changelog/A.1.1/to.md">A.1.0 -> A.1.1</a></li>
        <li><a href="./changelog/A.1.1/h1.md">A.1.1 hotfix 1</a></li>
        <li><a href="./changelog/B.1.0/to.md">A.1.1 -> B.1.0</a></li>
        <li><a href="./changelog/B.1.0/h1.md">B.1.0 hotfix 1</a></li>
    </ul>
