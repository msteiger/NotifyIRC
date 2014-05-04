/*
 * Copyright 2014 Martin Steiger
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.myirc;

import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.text.DateFormat;
import java.util.ArrayDeque;
import java.util.Date;
import java.util.Deque;

import org.jibble.pircbot.PircBot;

/**
 * @author Martin Steiger
 */
public class MyBot extends PircBot
{
	/**
	 * In milliseconds
	 */
	private static final int MAX_AGE = 5000;

	static final DateFormat TIME_FORMAT = DateFormat.getTimeInstance(DateFormat.SHORT);

	private TrayIcon trayIcon;

	private boolean showJoinsParts;
	private boolean showAllMessages;

	private static class Message
	{
		private String text;
		private Date time;

		public Message(String text, Date time)
		{
			this.text = text;
			this.time = time;
		}

		public Date getTime()
		{
			return time;
		}

		@Override
		public String toString()
		{
			return String.format("[%s] %s", TIME_FORMAT.format(time), text);
		}
	}

	private Deque<Message> messages = new ArrayDeque<>();

	/**
	 * @param trayIcon the tray icon to use
	 * @param name the name of the bot
	 */
	public MyBot(TrayIcon trayIcon, String name)
	{
		this.trayIcon = trayIcon;
		setName(name);
	    setLogin(name);
	    setVersion("1.0");
	    setFinger(name);
	}
	
	@Override
	protected void onJoin(String channel, String sender, String login, String hostname)
	{
		if (!showJoinsParts)
			return;

		display(String.format("%s has joined %s", sender, channel));
	}

	@Override
	protected void onPart(String channel, String sender, String login, String hostname)
	{
		if (!showJoinsParts)
			return;

		display(String.format("%s has left %s", sender, channel));
	}

	@Override
	public void onMessage(String channel, String sender, String login, String hostname, String message)
	{
		if (!showAllMessages)
			return;

		display(String.format("%s: %s", sender, message));
	}
	
	private void display(String message)
	{
		Date now = new Date();

		messages.addLast(new Message(message, now));

		while (true)
		{
			Message oldest = messages.peekFirst();
			if (oldest == null)
				break;

			long milliSecs = now.getTime() - oldest.getTime().getTime();
			if (milliSecs < MAX_AGE)
				break;

			messages.removeFirst();
		}

		StringBuilder sb = new StringBuilder();

		for (Message msg : messages)
		{
			if (sb.length() > 0)
				sb.append(System.lineSeparator());

			sb.append(msg.toString());
		}

		String title = null;
		trayIcon.displayMessage(title, sb.toString(), MessageType.INFO);
	}

	/**
	 * @param onoff true if all messages should be shown
	 */
	public void showAllMessages(boolean onoff)
	{
		showAllMessages = onoff;
	}

	/**
	 * @param onoff true if join/part messages should be shown
	 */
	public void showJoinsParts(boolean onoff)
	{
		showJoinsParts = onoff;
	}
}
