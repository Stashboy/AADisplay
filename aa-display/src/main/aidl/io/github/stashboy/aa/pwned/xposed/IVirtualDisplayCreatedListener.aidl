package io.github.stashboy.aa.pwned.xposed;

interface IVirtualDisplayCreatedListener {
    void onAvailableDisplay(int displayId, boolean create);
}