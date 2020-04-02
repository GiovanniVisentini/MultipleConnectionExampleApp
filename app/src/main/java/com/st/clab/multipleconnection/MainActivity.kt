/*
Copyright (c) 2020, 2020 STMicroelectronics
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

* Redistributions of source code must retain the above copyright notice, this
list of conditions and the following disclaimer.

* Redistributions in binary form must reproduce the above copyright notice,
this list of conditions and the following disclaimer in the documentation
and/or other materials provided with the distribution.

* Neither the name of test nor the names of its
contributors may be used to endorse or promote products derived from
this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package com.st.clab.multipleconnection

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.st.BlueSTSDK.Feature
import com.st.BlueSTSDK.Features.FeatureAcceleration
import com.st.BlueSTSDK.Manager
import com.st.BlueSTSDK.Node

class MainActivity : AppCompatActivity() {

    //first node data
    private lateinit var statusNode1:TextView
    private lateinit var dataNode1:TextView

    private var node1: Node?=null
    private val node1statusListener by lazy { NodeStatusListener(statusNode1) }
    private val node1DataListener by lazy { FeatureListener(dataNode1) }

    //second node data
    private lateinit var statusNode2:TextView
    private lateinit var dataNode2:TextView

    private var node2: Node?=null
    //we build it lazy to be secure that the text view are initialized
    private val node2statusListener by lazy { NodeStatusListener(statusNode2) }
    private val node2DataListener by lazy { FeatureListener(dataNode2) }

    //caputre the needed nodes and start the connection when both nodes are present
    private val managerListener = object:Manager.ManagerListener{
        override fun onDiscoveryChange(m: Manager, enabled: Boolean) {
        }

        override fun onNodeDiscovered(m: Manager, node: Node) {
            if(node.tag == NODE_1_TAG){
                node1=node
            }
            if(node.tag == NODE_2_TAG){
                node2=node
            }
            if(node1!=null && node2 !=null){
                m.stopDiscovery()
                connectToNodes()
            }
        }
    }

    //print the node status and enable the notification when the node connects
    inner class NodeStatusListener(private val text:TextView): Node.NodeStateListener{
        override fun onStateChange(node: Node, newState: Node.State, prevState: Node.State) {
            runOnUiThread {
                text.text = newState.toString()
            }
            if(newState==Node.State.Connected){
                val listener = if(node.tag == NODE_1_TAG){
                    node1DataListener
                }else{
                    node2DataListener
                }

                node.getFeature(FeatureAcceleration::class.java)?.apply {
                    addFeatureListener(listener)
                    enableNotification()
                }
            }
        }
    }

    //print the feature data
    inner class FeatureListener(private val text:TextView): Feature.FeatureListener{
        override fun onUpdate(f: Feature, sample: Feature.Sample) {
            //Log.d("Feature","node: ${f.parentNode.name}")
            runOnUiThread {
                text.text = sample.toString()
            }
        }

    }

    private fun connectToNodes() {
        node1?.apply {
            addNodeStateListener(node1statusListener)
            //use application to keep the node connected also if the activity gets destroyed
            connect(this@MainActivity.applicationContext)
        }

        node2?.apply {
            addNodeStateListener(node2statusListener)
            connect(this@MainActivity.applicationContext)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        statusNode1 = findViewById(R.id.node1_status)
        statusNode2 = findViewById(R.id.node2_status)

        dataNode1 = findViewById(R.id.node1_data)
        dataNode2 = findViewById(R.id.node2_data)
    }

    override fun onStart() {
        super.onStart()

        val manager = Manager.getSharedInstance()

        node1 = manager.getNodeWithTag(NODE_1_TAG)
        node2 = manager.getNodeWithTag(NODE_1_TAG)

        //no node discovered
        if(node1==null || node2==null){
            manager.apply {
                addListener(managerListener)
                //TODO ADD PERMISSION REQUEST
                // go to settings and enable manually the location permission to run the app
                startDiscovery()
            }
        }else{
            //node known enable the notification
            //assuming that are already connected
            node1?.getFeature(FeatureAcceleration::class.java)?.apply {
                addFeatureListener(node1DataListener)
                enableNotification()
            }
            node2?.getFeature(FeatureAcceleration::class.java)?.apply {
                addFeatureListener(node2DataListener)
                enableNotification()
            }
        }


    }

    override fun onStop() {
        super.onStop()
        //remove the listener and stop the trasmission
        node1?.apply {
            removeNodeStateListener(node1statusListener)
            getFeature(FeatureAcceleration::class.java)?.apply {
                removeFeatureListener(node1DataListener)
                disableNotification()
            }

        }

        node2?.apply {
            removeNodeStateListener(node2statusListener)
            getFeature(FeatureAcceleration::class.java)?.apply {
                removeFeatureListener(node2DataListener)
                disableNotification()
            }
        }

        //TODO: DISCONNECT THE NODES?
    }

    companion object{
        const val NODE_1_TAG="C0:84:5B:35:2D:33"
        const val NODE_2_TAG="C0:86:2B:37:3F:30"
    }
}
