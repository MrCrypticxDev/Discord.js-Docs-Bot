package everyos.discord.jsdocbot;

import java.util.Iterator;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import discord4j.core.DiscordClientBuilder;
import discord4j.core.event.EventDispatcher;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.presence.Activity;
import discord4j.core.object.presence.Presence;
import everyos.discord.jsdocbot.util.UnirestUtil;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import discord4j.rest.util.Color;

public class DJSDocBot {
	public static void main(String[] args) {
		final String token = System.getenv().get("DJS_KEY");
		if (token==null) {
			System.out.println("You must set the environment variable $DJS_KEY");
			System.exit(1);
		}
		new DJSDocBot(token);
	}
	
	public JsonObject j;
	String prefix = "-";
	public DJSDocBot(String token) {
		DiscordClientBuilder.create(token)
		.build()
        .withGateway(client->{
        	EventDispatcher dispatcher = client.getEventDispatcher();
        	
        	Mono<?> m0 = client.updatePresence(Presence.online(Activity.playing("Discord.JS Docs Bot | -help | Written in Discord4J")));
        	Flux<?> m1 = dispatcher.on(MessageCreateEvent.class)
            	.publishOn(Schedulers.boundedElastic())
            	.filter(e->e.getMessage().getContent().startsWith(prefix+"docs ")) //Too lazy to write an actual command handler
            	.flatMap(e->{
            		String content = e.getMessage().getContent().substring((prefix+"docs ").length());
            		Mono<JsonObject> jsonm = j!=null?Mono.just(j):null;
            		if (jsonm==null) jsonm = UnirestUtil.get("https://raw.githubusercontent.com/discordjs/discord.js/docs/stable.json", req->req)
            			.map(resp->{
            				j = JsonParser.parseString(resp.getBody()).getAsJsonObject();
            				return j;
            			});
            		return jsonm.flatMap(resp->{
            			return e.getMessage().getChannel().flatMap(channel->{
            				//TODO: Split content on dot, for methods
            				String[] types = new String[] {"classes", "typedefs", "externals"};
            				String type = null;
            				JsonObject info = null;
            				for (String typ: types) {
            					type = typ;
            					info = searchName(resp.get(typ).getAsJsonArray(), content);
            					if (info!=null) break;
            				}
            				
            				if (info==null) return channel.createMessage("No search results found!");
            				final JsonObject finfo = info;
            				
            				if (type.equals("externals")) {
            					return channel.createEmbed(spec->{
            						String see = finfo.get("see").getAsJsonArray().get(0).getAsString();
            						spec.setUrl(see.substring(7, see.length()-1));
            						spec.setTitle(finfo.get("name").getAsString());
            						JsonObject meta = finfo.get("meta").getAsJsonObject();
            						spec.setDescription(metaFrom(meta));
            					});
            				}
            				
            				if (type.equals("classes")) {
            					return channel.createEmbed(spec->{
            						spec.setTitle(finfo.get("name").getAsString());
            						spec.setDescription(finfo.get("description").getAsString());
									spec.setColor(Color.WHITE);
									spec.addField("Location", metaFrom(finfo.get("meta").getAsJsonObject()), false);
            						
            						if (finfo.has("construct")) {
            							JsonObject construct = finfo.get("construct").getAsJsonObject();
            							StringBuilder desc = new StringBuilder("`new "+construct.get("name").getAsString()+"(");
            							boolean isFirst = true;
            							JsonArray arr = construct.get("params").getAsJsonArray();
            							for (int i = 0; i<arr.size(); i++) {
            								JsonObject el2 = arr.get(i).getAsJsonObject();
            								desc.append(" ");
            								if (el2.has("optional")&&el2.get("optional").getAsBoolean()) desc.append("[");
            								if (!isFirst) {
            									desc.append(",");
            								} else {
            									isFirst = false;
            								}
            								desc.append(fromTypes(el2.get("type").getAsJsonArray(), "/")+" ");
            								desc.append(el2.get("name").getAsString());
            								if (el2.has("optional")&&el2.get("optional").getAsBoolean()) desc.append("]");
            							};
            							desc.append(" )`");
            							arr.forEach(el->{
            								JsonObject el2 = el.getAsJsonObject();
            								desc.append("\n**"+el2.get("name").getAsString()+"** - "+el2.get("description").getAsString());
            							});
            							spec.addField("Construct", desc.toString(), false);
            						}
            						
            						if (finfo.has("methods")) {
            							StringBuilder methods = new StringBuilder();
	            						finfo.get("methods").getAsJsonArray().forEach(el->{
	            							methods.append(", `"+el.getAsJsonObject().get("name").getAsString()+"`");
	            						});
	            						spec.addField("Methods", methods.toString().substring(2), false);
            						}
            						if (finfo.has("props")) {
            							StringBuilder properties = new StringBuilder();
	            						finfo.get("props").getAsJsonArray().forEach(el->{
	            							properties.append(", `"+el.getAsJsonObject().get("name").getAsString()+"`");
	            						});
	            						spec.addField("Properties", properties.toString().substring(2), false);
            						}
            						if (finfo.has("events")) {
            							StringBuilder events = new StringBuilder();
	            						finfo.get("events").getAsJsonArray().forEach(el->{
	            							events.append(", `"+el.getAsJsonObject().get("name").getAsString()+"`");
	            						});
	            						spec.addField("Events", events.toString().substring(2), false);
            						}
            					});
            				}
            				
            				if (type.equals("typedefs")) {
            					return channel.createEmbed(spec->{
            						spec.setTitle(finfo.get("name").getAsString());
            						spec.setDescription(finfo.get("description").getAsString());
            						spec.addField("Location", metaFrom(finfo.get("meta").getAsJsonObject()), false);
            						
            						String dtypes = fromTypes(finfo.get("type").getAsJsonArray(), "\n");
            						spec.addField("Matching Types", dtypes, false);
            					});
            				}
            				
            				return Mono.empty();
            			});
            		});
            	});
            	
            return Mono.when(m0.and(r(m1)))
            	.onErrorContinue((e, o)->e.printStackTrace());
        }).block();
	}
	
	private String fromTypes(JsonArray types, String sep) {
		StringBuilder dtypes = new StringBuilder();
		types.forEach(el->{
			dtypes.append(sep);
			el.getAsJsonArray().forEach(el2->{
				el2.getAsJsonArray().forEach(el3->{
					dtypes.append(el3.getAsString());
				});
			});
		});
		
		return dtypes.substring(sep.length());
	}

	public Flux<?> r(Flux<?> flux) {
    	return flux.onErrorContinue((e, o)->e.printStackTrace());
    }
	
	public JsonObject searchName(JsonArray arr, String name) {
		Iterator<JsonElement> it = arr.iterator();
		while (it.hasNext()) {
			JsonObject jo = it.next().getAsJsonObject();
			if (jo.get("name").getAsString().toLowerCase().equals(name.toLowerCase()))
				return jo;
		}
		return null;
	}
	
	public String metaFrom(JsonObject meta) {
		return
			"https://github.com/discordjs/discord.js/blob/master/"+
			meta.get("path").getAsString()+"/"+
			meta.get("file").getAsString()+"#L"+
			meta.get("line").getAsString();
	}
}
