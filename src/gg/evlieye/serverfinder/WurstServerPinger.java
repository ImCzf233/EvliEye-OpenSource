/*

 *



 */
package gg.evlieye.serverfinder;

import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicInteger;

import net.minecraft.client.network.MultiplayerServerListPinger;
import net.minecraft.client.network.ServerInfo;

public class EvlieyeServerPinger
{
	private static final AtomicInteger threadNumber = new AtomicInteger(0);
	private ServerInfo server;
	private boolean done = false;
	private boolean failed = false;
	
	public void ping(String ip)
	{
		ping(ip, 25565);
	}
	
	public void ping(String ip, int port)
	{
		server = new ServerInfo("", ip + ":" + port, false);
		
		new Thread(() -> pingInCurrentThread(ip, port),
			"Evlieye Server Pinger #" + threadNumber.incrementAndGet()).start();
	}
	
	private void pingInCurrentThread(String ip, int port)
	{
		MultiplayerServerListPinger pinger = new MultiplayerServerListPinger();
		System.out.println("Pinging " + ip + ":" + port + "...");
		
		try
		{
			pinger.add(server, () -> {});
			System.out.println("Ping successful: " + ip + ":" + port);
			
		}catch(UnknownHostException e)
		{
			System.out.println("Unknown host: " + ip + ":" + port);
			failed = true;
			
		}catch(Exception e2)
		{
			System.out.println("Ping failed: " + ip + ":" + port);
			failed = true;
		}
		
		pinger.cancel();
		done = true;
	}
	
	public boolean isStillPinging()
	{
		return !done;
	}
	
	public boolean isWorking()
	{
		return !failed;
	}
	
	public boolean isOtherVersion()
	{
		return server.protocolVersion != 47;
	}
	
	public String getServerIP()
	{
		return server.address;
	}
}
