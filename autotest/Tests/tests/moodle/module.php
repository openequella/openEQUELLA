<html>
<head><title>Upgrade moodle modules</title></head>
<body>

<?php

function resetGit() {
        shell_exec('git fetch');
        $branch = shell_exec('git rev-parse --symbolic-full-name --abbrev-ref HEAD');
        $result = shell_exec("git reset --hard origin/$branch 2>&1");
        echo $result.'<br>';

        flush();
        ob_flush();
}

ob_end_flush();
flush();
ob_flush();

$versions = array(25, 26, 27);
foreach ($versions as $version)
{
        echo "<p>Upgrading moodle$version<br>";
        flush();
        ob_flush();

        chdir("./moodle$version/mod/equella");
        resetGit();

        chdir("../../blocks/equella_search");
        resetGit();

        chdir("../equella_tasks");
        resetGit();
        chdir('../../../');
}

?>
<p id="success">Modules upgraded</p>
</body>
</html>

