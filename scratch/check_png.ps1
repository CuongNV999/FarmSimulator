Add-Type -AssemblyName System.Drawing
Get-ChildItem -Recurse -Filter *.png src/main/resources/assets/textures/Crops | ForEach-Object {
    try {
        $img = [System.Drawing.Image]::FromFile($_.FullName)
        $w = $img.Width
        $h = $img.Height
        $path = $_.FullName
        Write-Output "$path : ${w}x${h}"
        $img.Dispose()
    } catch {
        Write-Warning "Failed to load $_"
    }
}
