$ProjectRoot = [System.IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$ProjectCache = Join-Path $ProjectRoot '.cache'
foreach ($directory in @('tmp', 'npm', 'maven', 'cargo', 'rust-target', 'logs')) {
    New-Item -ItemType Directory -Force -Path (Join-Path $ProjectCache $directory) | Out-Null
}

# Keep downloads and temporary files inside this checkout. Installed tools are reused.
$env:TEMP = Join-Path $ProjectCache 'tmp'
$env:TMP = $env:TEMP
$env:npm_config_cache = Join-Path $ProjectCache 'npm'
$env:npm_config_update_notifier = 'false'
$env:CARGO_HOME = Join-Path $ProjectCache 'cargo'
$env:CARGO_TARGET_DIR = Join-Path $ProjectCache 'rust-target'
$env:CARGO_HTTP_MULTIPLEXING = 'false'
$env:RUSTUP_AUTO_INSTALL = '0'
$env:MAVEN_OPTS = ($env:MAVEN_OPTS + ' "-Dmaven.repo.local=' + (Join-Path $ProjectCache 'maven') + '" "-Djava.io.tmpdir=' + $env:TEMP + '"').Trim()

function Invoke-Checked {
    param([string]$Command, [string[]]$CommandArguments)
    & $Command @CommandArguments
    if ($LASTEXITCODE -ne 0) {
        throw "$Command failed with exit code $LASTEXITCODE"
    }
}
