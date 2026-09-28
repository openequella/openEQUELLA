# openEQUELLA Coordinated Vulnerability Process (CVP)

If you discover any security concerns with openEQUELLA or associated technology, please let the openEQUELLA Security Group know through one of the following channels:

- **GitHub Private Vulnerability Reporting** (preferred) - use [Report a vulnerability](https://github.com/openequella/openEQUELLA/security/advisories/new). Your report is visible only to you and the maintainers.
- **Email** - send details to <security@apereo.org>, or raise it through your commercial service partner.

Please do not raise security issues on the public issue tracker.

Reports made through GitHub arrive as a draft security advisory. For reports received by email, a team member of the openEQUELLA Security Group will open a draft advisory as needed - <https://github.com/openequella/openEQUELLA/security/advisories>

The openEQUELLA Security Group will then review the issue and help determine next steps. The team member that fielded the issue will respond to the reporter with the recommended path forward.

When deemed appropriate by the above review:

- An embargo date is chosen (when the issue will become public)
- A fix is created, ideally on the advisory's temporary private fork
- Where warranted, a CVE is requested through the advisory
- On the embargo date:
  - The fix is released
  - The advisory is published, crediting the reporter unless they prefer otherwise
  - Notices are sent to affected parties, typically via commercial service partners and, where appropriate, the [equella-users](https://groups.google.com/a/apereo.org/g/equella-users) mailing list

The openEQUELLA Security Group is not responsible for fixing a given security issue. They are responsible to do the initial review, recommend a path forward, and guide the advisory to completion.

## Supported versions

The openEQUELLA Security Group focuses on the [latest release](https://github.com/openequella/openEQUELLA/releases/latest). Please confirm an issue still reproduces there before reporting. Whether a fix is also made available for older releases is decided case by case.
