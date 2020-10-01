const Discord = require('discord.js');
const client = new Discord.Client();
const prefix = '-'
const Games = [`Docs Bot | -help`, `YOUR_STATUS`]
    setInterval(() => { client.user.setActivity(`${Games[Math.floor(Math.random() * Games.length)]}`) }, 10000)
const Constants = require('discord.js/src/util/Constants.js')
Constants.DefaultOptions.ws.properties.$browser = `Discord iOS`

client.on('message', async message => {
const args = message.content
      .slice(message.guild.commandPrefix.length)
      .split(" ");
    if (query) {
      let source = sources.includes(args[args.length - 1])
        ? args.pop()
        : "stable";
      if (source == "11.5-dev")
        source = `https://discord.js.org/#/docs/${source}.json`;
        
        (async () => {
            const { data } =  await get(`https://discord.js.org/#/docs`, {
              params: { src: source, q: query }
            });
          })()
      const embed = new MessageEmbed(data)
        .setTitle("Discord.Js Documentation")
        .setFooter(
          `Thanks to ${
            this.client.users.get("542058542853521461").tag
          } for letting me use this command`
        );
      message.channel.send({ embed: embed });
    }
})
client.on('ready', () => {
    //logs "Connected" in green
    console.log('\x1b[32m%s\x1b[0m',
    `Connected as ${client.user.tag}`);
    //sets bot status to red
    //client.user.setStatus('idle');
    //client.user.setActivity('Discord.js Underground Community');
    //client.user.setActivity(`Discord.js Underground Community`, { type: 'WATCHING' })
})

client.on('guildMemberAdd', member => {
    // Send the message to a designated channel on a server:
    const channel = member.guild.channels.find(ch => ch.name === 'user-logs');
    // Do nothing if the channel wasn't found on this server
    if (!channel) return;
    channel.send(`YOUR_STATUS, Thanks for joining with us! ${member}`);
});
    

client.on('message', message => {
    let args = message.content.split(' ')
    let color = '#'+(0x1000000+(Math.random())*0xffffff).toString(16).substr(1,6)
    if (message.content.toLowerCase() === `${prefix}ping`) {
        message.channel.send('Pinging...').then(sent => {
            let time1 = sent.createdTimestamp - message.createdTimestamp
            let time2 = Math.round(client.ping)
            let embed = new Discord.RichEmbed()
                .setAuthor('Pong!', client.user.displayAvatarURL)
                .setColor(color)
                .setDescription(`**Bot Latency:** \`${time1}ms\`\n**API Latency:** \`${time2}ms\``)
                .setTimestamp()
            sent.edit(embed)
        })
    }
    if (message.content.toLowerCase() === `${prefix}help`) {
        let embed = new Discord.RichEmbed()
            .setDescription(`\`${prefix}help\` - Shows a list of commands.\n\`${prefix}ping\` - Pong! Get the bot's ping!\n\`${prefix}docs\` - Documentation of Discord.js.\n\`${prefix}search\` - Topics of Discord.js`)
            .setColor(color)
            .setFooter(`Author: ᴍʀᴄʀʏᴘᴛɪᴄx & EveryOS`, client.user.displayAvatarURL)
            .setTimestamp()
        message.channel.send(embed)
    
    }
})
client.login('YOUR_DISCRD_BOT_TOKEN');
