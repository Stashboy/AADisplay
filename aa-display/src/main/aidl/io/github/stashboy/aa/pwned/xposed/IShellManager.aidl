package io.github.stashboy.aa.pwned.xposed;

interface IShellManager {
    boolean createVirtualDisplayBefore();
    boolean destroyVirtualDisplayAfter();
}