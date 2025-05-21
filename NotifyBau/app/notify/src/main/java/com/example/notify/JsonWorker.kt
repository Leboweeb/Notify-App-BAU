package com.example.notify

//class JsonWorker(
//    private val context: Context,
//    private var workParams: WorkerParameters,
//    endpoint: String
//) :
//    Worker(context, workParams) {
//    private var jsonInterface: JsonInterface = JsonInterface(this.context)
//
//
//    override fun doWork(): Result {
//
//        return completeFuture(jsonInterface.connectToApi(), jsonInterface)
////        val interfaces = listOf(
////            JsonInterface(this.context, "moodle/notifications"),
////            JsonInterface(this.context, "moodle/calendar")
////        )
////        val futures = ArrayList<CompletableFuture<Response>>()
////        for (i in interfaces) {
////            futures.add(i.connectToApi(JsonInterface.HTTPMethod.POST, body = i.getCachedJson()))
////        }
////        val futureResults =
////            futures.mapIndexed { index, future -> completeFuture(future, interfaces[index]) }
////
////        return if (futureResults.contains(Result.retry())) {
////            Result.retry()
////        } else if (futureResults.all { result -> result == Result.success() }) {
////            Result.success()
////        } else {
////            Result.failure()
////        }
//
//
//    }
//
//    private fun completeFuture(
//        future: CompletableFuture<Response>,
//        jsonInterface: JsonInterface
//    ): Result {
//        var response = future.get()
//        if (response.isSuccessful) {
//            // we succeeded first try, just return
//            return Result.success()
//        } else if (response.code == 400 && workParams.runAttemptCount < 1) {
//            // retry 2 times, we have an invalid session key
//            response = jsonInterface.connectToApi(
//                HTTPMethod.GET
//            ).get()
//            if (!response.isSuccessful)
//            // if we still haven't succeeded, try again
//                return Result.retry()
//
//            return Result.success()
//        } else {
//            return Result.failure()
//        }
//    }
//
//    override fun onStopped() {
//        super.onStopped()
//        Log.e(this.javaClass.simpleName, "Background worker stopped!")
//    }
//}