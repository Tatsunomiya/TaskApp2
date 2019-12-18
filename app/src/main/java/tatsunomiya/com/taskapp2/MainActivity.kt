package tatsunomiya.com.taskapp2

import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.realm.Realm
import io.realm.RealmChangeListener
import io.realm.Sort
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


const val EXTRA_TASK = "jp.tatsunomiya.com.taskapp2.TASK."

class MainActivity : AppCompatActivity() {


    private lateinit var mRealm: Realm

    private val mRealmListener = object : RealmChangeListener<Realm> {

          override fun onChange(element: Realm) {
              reloadListView()

    }

}

    private lateinit var mTaskAdapter: TaskAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            val intent = Intent(this@MainActivity, InputActivity::class.java)
            startActivity(intent)
        }
            mRealm = Realm.getDefaultInstance()
            mRealm.addChangeListener(mRealmListener)

            mTaskAdapter = TaskAdapter(this@MainActivity)



    button1.setOnClickListener{
        search()
    }

        listView1.setOnItemClickListener{
            parent,_,position,_ ->
            val task = parent.adapter.getItem(position) as Task
            val intent = Intent(this@MainActivity,InputActivity::class.java)
            intent.putExtra(EXTRA_TASK,task.id)
            startActivity(intent)
        }



        // ListViewを長押ししたときの処理

        listView1.setOnItemLongClickListener { parent, _, position, _ ->
            // タスクを削除する
            val task = parent.adapter.getItem(position) as Task

            // ダイアログを表示する
            val builder = AlertDialog.Builder(this@MainActivity)

            builder.setTitle("削除")
            builder.setMessage(task.title + "を削除しますか")

            builder.setPositiveButton("OK"){_, _ ->
                val results = mRealm.where(Task::class.java).equalTo("id", task.id).findAll()

                mRealm.beginTransaction()
                results.deleteAllFromRealm()
                mRealm.commitTransaction()


                val resultIntent = Intent(applicationContext,TaskAlarmReceiver::class.java)
                val resultPendingIntent = PendingIntent.getBroadcast(
                    this@MainActivity,
                    task.id,
                resultIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT


                )

                val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
                alarmManager.cancel(resultPendingIntent)

                reloadListView()
            }
            builder.setNegativeButton("CANCEL", null)

            val dialog = builder.create()
            dialog.show()

            true
        }


//        listView1.setOnItemLongClickListener { parent, _, position, _ ->
//            val task = parent.adapter.getItem(position) as Task
//
//            val builder = AlertDialog.Builder(this@MainActivity)
//
//            builder.setTitle("削除")
//            builder.setMessage(task.title + "を削除しますか")

//            builder.setPositiveButton("OK") {_, _ ->
//                  val results = mRealm.where(Task::class.java).equalTo("id",task.id).findAll()
//
//                      mRealm.beginTransaction()
//                      results.deleteAllFromRealm()
//                      mRealm.commitTransaction()
//
//                      reloadListView()
//
//                  }







        addTaskForTest()
        reloadListView()



    }

    private fun reloadListView() {
        // 後でTaskクラスに変更する
     val taskRealmResults = mRealm.where(Task:: class.java).findAll().sort("date", Sort.DESCENDING)
        mTaskAdapter.taskList = mRealm.copyFromRealm(taskRealmResults)
        listView1.adapter = mTaskAdapter
        mTaskAdapter.notifyDataSetChanged()
    }


    private fun addTaskForTest() {
        val task = Task()

        task.title="作業"
        task.contents = "プログラムを書いてPUSHする"
        task.date = Date()
        task.category = "カテゴリー"
        task.id= 0
        mRealm.beginTransaction()
        mRealm.copyToRealmOrUpdate(task)
        mRealm.commitTransaction()

    }


    private fun search() {
        val categoryserch= category_edit_text2.text.toString()



        val taskRealmResults = mRealm.where(Task:: class.java).contains("category",categoryserch).findAll().sort("date", Sort.DESCENDING)
        mTaskAdapter.taskList = mRealm.copyFromRealm(taskRealmResults)
        listView1.adapter = mTaskAdapter
        mTaskAdapter.notifyDataSetChanged()
    }




    override fun onDestroy() {
        super.onDestroy()
        mRealm.close()
    }


//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        menuInflater.inflate(R.menu.menu_main, menu)
//        return true
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        return when (item.itemId) {
//            R.id.action_settings -> true
//            else -> super.onOptionsItemSelected(item)
//        }
    }

