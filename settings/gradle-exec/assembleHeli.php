<?php

$mode = isset($argv[1]) ? $argv[1] : 'release';

include ('updateRevision.php');
include ('buildHelpText.php');
include ('createHelpPDFLinksHeli.php');
include ('info.php');



