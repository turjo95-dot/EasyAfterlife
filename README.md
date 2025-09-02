# ⚰️ EasyAfterlife - Secure Death, Smarter Recovery

![Version](https://img.shields.io/badge/version-1.0.0-blue)
![Minecraft](https://img.shields.io/badge/minecraft-1.20.4-green)
![License](https://img.shields.io/badge/license-MIT-yellow)

## 🌟 Overview

**EasyAfterlife** revolutionizes Minecraft death mechanics with a secure Grave Chest & Key system. When players die, their items and XP are safely stored in protected graves that can only be accessed with UUID-bound grave keys.

## ✨ Key Features

- **🔒 Secure Grave System**: Items stored in protected chests that cannot be broken or accessed without proper keys
- **🗝️ UUID-Bound Keys**: Grave keys are tied to player UUIDs, preventing theft and ensuring security
- **🛡️ Safety Mechanisms**: Smart relocation from dangerous areas (lava, void) to safe locations
- **⚙️ Highly Configurable**: Extensive configuration options for different server types
- **🎮 PvP Integration**: Optional raiding modes and corpse loot chances
- **🌍 Multi-World Support**: Enable/disable per world with flexible world management
- **📊 Economy Integration**: Vault support for key retrieval costs
- **🎯 Admin Tools**: Comprehensive admin commands for management
- **📈 PlaceholderAPI Support**: Integration with scoreboards and TAB plugins

## 🚀 Installation

1. Download the latest release from [Releases](https://github.com/turjo/easyafterlife/releases)
2. Place `EasyAfterlife-1.0.0.jar` in your server's `plugins` folder
3. Restart your server
4. Configure the plugin in `plugins/EasyAfterlife/config.yml`
5. Reload with `/graveadmin reload`

## 📋 Commands

### Player Commands
- `/grave locate` - Show coordinates of your last grave
- `/grave list` - List all your active graves  
- `/grave retrieve` - Retrieve a lost grave key

### Admin Commands
- `/graveadmin givekey <player>` - Give a grave key to a player
- `/graveadmin reload` - Reload plugin configuration
- `/graveadmin stats` - Show plugin statistics

## 🔐 Permissions

```yaml
# Full access
easyafterlife.*

# Player permissions
easyafterlife.player.use      # Basic grave commands (default: true)
easyafterlife.player.locate   # Locate graves (default: true)
easyafterlife.player.list     # List graves (default: true)
easyafterlife.player.retrieve # Retrieve keys (default: true)

# Admin permissions  
easyafterlife.admin.use       # Admin commands (default: op)
easyafterlife.admin.givekey   # Give keys (default: op)
easyafterlife.admin.reload    # Reload config (default: op)
```

## ⚙️ Configuration

The plugin comes with extensive configuration options. See `config.yml` for detailed settings including:

- Grave lifetime and behavior
- Key system settings
- PvP and raiding options
- Visual effects and sounds
- World restrictions
- Database settings
- Custom messages

## 🔌 Dependencies

### Required
- **Spigot/Paper** 1.20.4+
- **Java** 17+

### Optional
- **Vault** - Economy integration for key retrieval costs
- **HolographicDisplays** - Floating text above graves
- **PlaceholderAPI** - Placeholder support for other plugins

## 🛠️ Building from Source

```bash
git clone https://github.com/turjo/easyafterlife.git
cd easyafterlife
mvn clean package
```

The compiled JAR will be available in the `target/` directory.

## 📊 PlaceholderAPI Placeholders

- `%easyafterlife_graves_active%` - Number of active graves for the player
- `%easyafterlife_key_count%` - Number of grave keys the player has
- `%easyafterlife_total_graves%` - Total graves on the server
- `%easyafterlife_has_grave%` - Whether the player has an active grave (true/false)

## 🎯 Server Compatibility

Perfect for:
- **Survival Servers** - Clean death mechanics without item loss
- **Lifesteal Servers** - Balanced PvP with optional raiding
- **Hardcore Servers** - Fair recovery system
- **Skyblock Servers** - Prevent items falling into void
- **Prison Servers** - Secure item storage system

## 🐛 Support & Issues

- **Issues**: [GitHub Issues](https://github.com/turjo/easyafterlife/issues)
- **Discord**: [Support Server](https://discord.gg/your-server)
- **Documentation**: [Wiki](https://github.com/turjo/easyafterlife/wiki)

## 📄 License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

## 🙏 Credits

Created with ❤️ by **Turjo**

Special thanks to the Minecraft community for inspiration and feedback!

---

*⚰️ Make death meaningful, make recovery secure.*