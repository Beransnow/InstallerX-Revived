package com.rosan.installer.data.app.model.exception

class InstallFailedMissingInstallPermissionException : Exception {
    constructor() : super()

    constructor(message: String?) : super(message)
}