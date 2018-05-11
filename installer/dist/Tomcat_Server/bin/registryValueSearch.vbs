function readFromRegistry (strRegistryKey, strDefault )
    Dim WSHShell, value

    On Error Resume Next
    Set WSHShell = CreateObject("WScript.Shell")
    value = WSHShell.RegRead( strRegistryKey )

    if err.number <> 0 then
        readFromRegistry= strDefault
    else
        readFromRegistry=value
    end if

    set WSHShell = nothing
end function

value = readFromRegistry("HKEY_LOCAL_MACHINE\Software\Solarflare Communications, Inc.\Solarflare vSphere client plugin\InstallDir","Not")

wscript.echo value